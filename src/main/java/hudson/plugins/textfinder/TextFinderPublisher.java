package hudson.plugins.textfinder;

import static hudson.Util.fixEmpty;

import hudson.Extension;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.console.ConsoleNote;
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

import jenkins.MasterToSlaveFileCallable;
import jenkins.tasks.SimpleBuildStep;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Text Finder plugin for Jenkins. Search in the workspace using a regular expression and determine
 * build outcome based on matches.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class TextFinderPublisher extends Recorder implements Serializable, SimpleBuildStep {

    public String fileSet;
    public final String regexp;
    public boolean succeedIfFound;
    public boolean unstableIfFound;
    public boolean notBuiltIfFound;
    /** True to also scan the whole console output */
    public boolean alsoCheckConsoleOutput;

    @DataBoundConstructor
    public TextFinderPublisher(String regexp) {
        this.regexp = regexp;

        // Attempt to compile regular expression
        try {
            Pattern.compile(regexp);
        } catch (PatternSyntaxException e) {
            // falls through
        }
    }

    @Deprecated
    public TextFinderPublisher(
            String fileSet,
            String regexp,
            boolean succeedIfFound,
            boolean unstableIfFound,
            boolean alsoCheckConsoleOutput) {
        this(regexp);
        this.fileSet = Util.fixEmpty(fileSet.trim());
        this.succeedIfFound = succeedIfFound;
        this.unstableIfFound = unstableIfFound;
        this.alsoCheckConsoleOutput = alsoCheckConsoleOutput;
    }

    @DataBoundSetter
    public void setFileSet(String fileSet) {
        this.fileSet = Util.fixEmpty(fileSet.trim());
    }

    @DataBoundSetter
    public void setSucceedIfFound(boolean succeedIfFound) {
        this.succeedIfFound = succeedIfFound;
    }

    @DataBoundSetter
    public void setUnstableIfFound(boolean unstableIfFound) {
        this.unstableIfFound = unstableIfFound;
    }

    @DataBoundSetter
    public void setNotBuiltIfFound(boolean notBuiltIfFound) {
        this.notBuiltIfFound = notBuiltIfFound;
    }

    @DataBoundSetter
    public void setAlsoCheckConsoleOutput(boolean alsoCheckConsoleOutput) {
        this.alsoCheckConsoleOutput = alsoCheckConsoleOutput;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        findText(run, workspace, listener);
    }

    /** Indicates an orderly abortion of the processing. */
    private static final class AbortException extends RuntimeException {}

    private void findText(Run<?, ?> run, FilePath workspace, TaskListener listener)
            throws IOException, InterruptedException {
        try {
            PrintStream logger = listener.getLogger();
            boolean foundText = false;

            if (alsoCheckConsoleOutput) {
                // Do not mention the pattern we are looking for to avoid false positives
                logger.println("[Text Finder] Scanning console output...");
                foundText |= checkConsole(run, compilePattern(logger, regexp), logger);
                logger.println(
                        "[Text Finder] Finished looking for pattern "
                                + "'"
                                + regexp
                                + "'"
                                + " in the console output");
            }

            final RemoteOutputStream ros = new RemoteOutputStream(logger);

            if (fileSet != null) {
                logger.println(
                        "[Text Finder] Looking for pattern "
                                + "'"
                                + regexp
                                + "'"
                                + " in the files at "
                                + "'"
                                + fileSet
                                + "'");
                foundText |= workspace.act(new FileChecker(ros, fileSet, regexp));
            }

            if (foundText != succeedIfFound) {
                final Result finalResult;
                if (notBuiltIfFound) {
                    finalResult = Result.NOT_BUILT;
                } else {
                    finalResult = unstableIfFound ? Result.UNSTABLE : Result.FAILURE;
                }
                run.setResult(finalResult);
            }
        } catch (AbortException e) {
            // no test file found
            run.setResult(Result.UNSTABLE);
        }
    }

    /**
     * Search the given regexp pattern.
     *
     * @param isConsoleLog True if the reader represents a console log (as opposed to a file).
     */
    private static boolean checkPattern(
            Reader r, Pattern pattern, PrintStream logger, String header, boolean isConsoleLog)
            throws IOException {
        boolean logFilename = true;
        boolean foundText = false;
        try (BufferedReader reader = new BufferedReader(r)) {
            // Assume default encoding and text files
            String line;
            while ((line = reader.readLine()) != null) {
                /*
                 * Strip console logs of their console notes before searching; otherwise, we might
                 * accidentally match the search string in the encoded console note.
                 */
                if (isConsoleLog) {
                    line = ConsoleNote.removeNotes(line);
                }
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    if (logFilename) { // first occurrence
                        if (header != null) {
                            logger.println(header);
                        }
                        logFilename = false;
                    }
                    logger.println(line);
                    foundText = true;
                    /*
                     * When searching console output, return immediately as soon as the first hit is
                     * found; otherwise, we'll loop forever.
                     */
                    if (isConsoleLog) {
                        return true;
                    }
                }
            }
        }
        return foundText;
    }

    private static boolean checkConsole(Run<?, ?> build, Pattern pattern, PrintStream logger) {
        try (Reader r = build.getLogReader()) {
            return checkPattern(r, pattern, logger, null, true);
        } catch (IOException e) {
            logger.println("[Text Finder] Error reading console output -- ignoring");
            Functions.printStackTrace(e, logger);
        }

        return false;
    }

    private static boolean checkFile(File f, Pattern pattern, PrintStream logger, Charset charset) {
        try (InputStream is = new FileInputStream(f);
                Reader r = new InputStreamReader(is, charset)) {
            return checkPattern(r, pattern, logger, f + ":", false);
        } catch (IOException e) {
            logger.println("[Text Finder] Error reading file '" + f + "' -- ignoring");
            Functions.printStackTrace(e, logger);
        }

        return false;
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

        /**
         * Checks the regular expression validity.
         *
         * @param value The expression to check
         * @return The form validation result
         */
        public FormValidation doCheckRegexp(@QueryParameter String value) {
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
            PrintStream logger = new PrintStream(ros, true, Charset.defaultCharset().toString());

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

                foundText |= checkFile(f, pattern, logger, Charset.defaultCharset());
            }

            return foundText;
        }
    }

    private static final long serialVersionUID = 1L;
}
