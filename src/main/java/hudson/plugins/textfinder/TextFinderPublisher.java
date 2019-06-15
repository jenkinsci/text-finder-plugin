package hudson.plugins.textfinder;

import static hudson.Util.fixEmpty;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.servlet.ServletException;
import jenkins.MasterToSlaveFileCallable;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Text Finder plugin for Jenkins. Search in the workspace using a regular expression and determine
 * build outcome based on matches.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class TextFinderPublisher extends Recorder implements Serializable, SimpleBuildStep {

    /** This is the primary text finder in the configuration. */
    private final TextFinderModel primaryTextFinder;

    /** Additional text finder configurations are stored here. */
    private final List<TextFinderModel> additionalTextFinders;

    /**
     * @param fileSet Kept for backward compatibility with old configuration.
     * @param regexp Kept for backward compatibility with old configuration.
     * @param succeedIfFound Kept for backward compatibility with old configuration.
     * @param unstableIfFound Kept for backward compatibility with old configuration.
     * @param notBuiltIfFound Kept for backward compatibility with old configuration.
     * @param alsoCheckConsoleOutput Kept for backward compatibility with old configuration.
     * @param additionalTextFinders configuration for additional textFinders
     */
    @DataBoundConstructor
    public TextFinderPublisher(
            String fileSet,
            String regexp,
            boolean succeedIfFound,
            boolean unstableIfFound,
            boolean notBuiltIfFound,
            boolean alsoCheckConsoleOutput,
            List<TextFinderModel> additionalTextFinders) {
        this.primaryTextFinder =
                new TextFinderModel(
                        Util.fixEmpty(fileSet != null ? fileSet.trim() : ""),
                        regexp,
                        succeedIfFound,
                        unstableIfFound,
                        alsoCheckConsoleOutput,
                        notBuiltIfFound);

        this.additionalTextFinders = new ArrayList<>();
        if (additionalTextFinders != null && !additionalTextFinders.isEmpty()) {
            this.additionalTextFinders.addAll(additionalTextFinders);
        }

        // Attempt to compile regular expressions
        try {
            Pattern.compile(primaryTextFinder.getRegexp());
            for (TextFinderModel textFinder : this.additionalTextFinders) {
                Pattern.compile(textFinder.getRegexp());
            }
        } catch (PatternSyntaxException e) {
            // falls through
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        findText(primaryTextFinder, run, workspace, listener);
        for (TextFinderModel additionalTextFinder : additionalTextFinders) {
            findText(additionalTextFinder, run, workspace, listener);
        }
    }

    /** Indicates an orderly abortion of the processing. */
    private static final class AbortException extends RuntimeException {}

    private void findText(
            TextFinderModel textFinder, Run<?, ?> run, FilePath workspace, TaskListener listener)
            throws IOException, InterruptedException {
        try {
            PrintStream logger = listener.getLogger();
            boolean foundText = false;

            if (textFinder.isAlsoCheckConsoleOutput()) {
                // Do not mention the pattern we are looking for to avoid false positives
                logger.println("[Text Finder] Scanning console output...");
                foundText |=
                        checkFile(
                                run.getLogFile(),
                                compilePattern(logger, textFinder.getRegexp()),
                                logger,
                                run.getCharset(),
                                true);
                logger.println(
                        "[Text Finder] Finished looking for pattern "
                                + "'"
                                + textFinder.getRegexp()
                                + "'"
                                + " in the console output");
            }

            final RemoteOutputStream ros = new RemoteOutputStream(logger);

            if (textFinder.getFileSet() != null) {
                logger.println(
                        "[Text Finder] Looking for pattern "
                                + "'"
                                + textFinder.getRegexp()
                                + "'"
                                + " in the files at "
                                + "'"
                                + textFinder.getFileSet()
                                + "'");
                foundText |=
                        workspace.act(
                                new FileChecker(
                                        ros, textFinder.getFileSet(), textFinder.getRegexp()));
            }

            if (foundText != textFinder.isSucceedIfFound()) {
                final Result finalResult;
                if (textFinder.isNotBuiltIfFound()) {
                    finalResult = Result.NOT_BUILT;
                } else {
                    finalResult = textFinder.isUnstableIfFound() ? Result.UNSTABLE : Result.FAILURE;
                }
                run.setResult(finalResult);
            }
        } catch (AbortException e) {
            // no test file found
            run.setResult(Result.UNSTABLE);
        }
    }

    /**
     * Search the given regexp pattern in the file.
     *
     * @param abortAfterFirstHit true to return immediately as soon as the first hit is found. this
     *     is necessary when we are scanning the console output, because otherwise we'll loop
     *     forever.
     */
    private static boolean checkFile(
            File f,
            Pattern pattern,
            PrintStream logger,
            Charset charset,
            boolean abortAfterFirstHit) {
        boolean logFilename = true;
        boolean foundText = false;
        BufferedReader reader = null;
        try {
            // Assume default encoding and text files
            String line;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), charset));
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    if (logFilename) { // first occurrence
                        logger.println(f + ":");
                        logFilename = false;
                    }
                    logger.println(line);
                    foundText = true;
                    if (abortAfterFirstHit) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            logger.println("[Text Finder] Error reading file '" + f + "' -- ignoring");
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return foundText;
    }

    private static Pattern compilePattern(PrintStream logger, String regexp) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(regexp);
        } catch (PatternSyntaxException e) {
            logger.println("[Text Finder] Unable to compile regular expression '" + regexp + "'");
            throw new AbortException();
        }
        return pattern;
    }

    /** cut off first item as it is provided with getters */
    @SuppressWarnings("unused")
    public List<TextFinderModel> getAdditionalTextFinders() {
        return additionalTextFinders;
    }

    // backward compatibility getters below
    // all these getters are there for backward compatibility
    // get first field from list which is used to store original config values
    @SuppressWarnings("unused")
    public String getFileSet() {
        return this.primaryTextFinder.getFileSet();
    }

    @SuppressWarnings("unused")
    public String getRegexp() {
        return this.primaryTextFinder.getRegexp();
    }

    @SuppressWarnings("unused")
    public boolean isSucceedIfFound() {
        return this.primaryTextFinder.isSucceedIfFound();
    }

    @SuppressWarnings("unused")
    public boolean isUnstableIfFound() {
        return this.primaryTextFinder.isUnstableIfFound();
    }

    @SuppressWarnings("unused")
    public boolean isNotBuiltIfFound() {
        return this.primaryTextFinder.isNotBuiltIfFound();
    }

    @SuppressWarnings("unused")
    public boolean isAlsoCheckConsoleOutput() {
        return this.primaryTextFinder.isAlsoCheckConsoleOutput();
    }
    // backward compatibility getters above

    @Symbol("findText")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        @Override
        public String getDisplayName() {
            return Messages.TextFinderPublisher_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/text-finder/help.html";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public List<TextFinderModel.DescriptorImpl> getItemDescriptors() {
            Jenkins jenkins = Jenkins.getInstance();
            if (jenkins != null) {
                return jenkins.getDescriptorList(TextFinderModel.class);
            } else {
                throw new NullPointerException("not able to get Jenkins instance");
            }
        }

        /**
         * Checks the regular expression validity.
         *
         * @param value The expression to check
         * @return The form validation result
         * @throws IOException For backwards compatibility
         * @throws ServletException For backwards compatibility
         */
        public FormValidation doCheckRegexp(@QueryParameter String value)
                throws IOException, ServletException {
            value = fixEmpty(value);
            if (value == null) {
                return FormValidation.ok(); // not entered yet
            }

            try {
                Pattern.compile(value);
                return FormValidation.ok();
            } catch (PatternSyntaxException e) {
                return FormValidation.error(e.getMessage());
            }
        }
    }

    private static class FileChecker extends MasterToSlaveFileCallable<Boolean> {

        private final RemoteOutputStream ros;
        private final String fileSet;
        private final String regexp;

        public FileChecker(RemoteOutputStream ros, String fileSet, String regexp) {
            this.ros = ros;
            this.fileSet = fileSet;
            this.regexp = regexp;
        }

        @Override
        public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
            PrintStream logger = new PrintStream(ros, false, Charset.defaultCharset().toString());

            // Collect list of files for searching
            FileSet fs = new FileSet();
            Project p = new Project();
            fs.setProject(p);
            fs.setDir(ws);
            fs.setIncludes(fileSet);
            DirectoryScanner ds = fs.getDirectoryScanner(p);

            // Any files in the final set?
            String[] files = ds.getIncludedFiles();
            if (files.length == 0) {
                logger.println("[Text Finder] File set '" + fileSet + "' is empty");
                throw new AbortException();
            }

            Pattern pattern = compilePattern(logger, regexp);

            boolean foundText = false;

            for (String file : files) {
                File f = new File(ws, file);

                if (!f.exists()) {
                    logger.println("[Text Finder] Unable to find file '" + f + "'");
                    continue;
                }

                if (!f.canRead()) {
                    logger.println("[Text Finder] Unable to read from file '" + f + "'");
                    continue;
                }

                foundText |= checkFile(f, pattern, logger, Charset.defaultCharset(), false);
            }

            return foundText;
        }
    }

    private static final long serialVersionUID = 1L;
}
