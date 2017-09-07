package hudson.plugins.textfinder;

import hudson.Extension;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static hudson.Util.fixEmpty;

/**
 * Text Finder plugin for Jenkins. Search in the workspace using a regular
 * expression and determine build outcome based on matches.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class TextFinderPublisher extends Recorder implements Serializable {
    /* these are kept here just for backward compatibility */
    public final String fileSet;
    public final String regexp;
    public final boolean succeedIfFound;
    public final boolean unstableIfFound;
    public final boolean notBuiltIfFound;
    public final boolean alsoCheckConsoleOutput;


    private List<TextFinderModel> textFinders;

    @DataBoundConstructor
    public TextFinderPublisher(String fileSet, String regexp, boolean succeedIfFound, boolean unstableIfFound,
                               boolean alsoCheckConsoleOutput, boolean notBuiltIfFound, List<TextFinderModel>
                                       textFinders) {
        this.textFinders = textFinders;
        this.fileSet = fileSet;
        this.regexp = regexp;
        this.succeedIfFound = succeedIfFound;
        this.unstableIfFound = unstableIfFound;
        this.notBuiltIfFound = notBuiltIfFound;
        this.alsoCheckConsoleOutput = alsoCheckConsoleOutput;

        try {
            Pattern.compile(regexp);
        } catch (PatternSyntaxException e) {
            // falls through
        }
    }

    public List<TextFinderModel> getTextFinders() {
        return textFinders;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws
            InterruptedException, IOException {
        findText(build, listener.getLogger(), new TextFinderModel(fileSet, regexp, succeedIfFound,
                unstableIfFound, alsoCheckConsoleOutput, notBuiltIfFound));
        if (textFinders != null) {
            for (TextFinderModel textFinder : textFinders) {
                findText(build, listener.getLogger(), textFinder);
            }
        }
        return true;
    }

    /**
     * Indicates an orderly abortion of the processing.
     */
    private static final class AbortException extends RuntimeException {
    }


    private void findText(AbstractBuild build, PrintStream logger, final TextFinderModel textFinder) throws IOException,
            InterruptedException {
        try {
            boolean foundText = false;

            if (textFinder.alsoCheckConsoleOutput) {
                logger.println("Checking console output");
                foundText |= checkFile(build.getLogFile(), compilePattern(logger, textFinder), logger, true);
            } else {
                // printing this when checking console output will cause the plugin
                // to find this line, which would be pointless.
                // doing this only when fileSet!=null to avoid
                logger.println("Checking " + textFinder.regexp);
            }

            final RemoteOutputStream ros = new RemoteOutputStream(logger);

            if (textFinder.fileSet != null) {
                foundText |= build.getWorkspace().act(new FileCallable<Boolean>() {
                    @Override
                    public void checkRoles(RoleChecker checker) throws SecurityException {

                    }

                    public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
                        PrintStream logger = new PrintStream(ros);

                        // Collect list of files for searching
                        FileSet fs = new FileSet();
                        org.apache.tools.ant.Project p = new org.apache.tools.ant.Project();
                        fs.setProject(p);
                        fs.setDir(ws);
                        fs.setIncludes(textFinder.fileSet);
                        DirectoryScanner ds = fs.getDirectoryScanner(p);

                        // Any files in the final set?
                        String[] files = ds.getIncludedFiles();
                        if (files.length == 0) {
                            logger.println("Jenkins Text Finder: File set '" +
                                    textFinder.fileSet + "' is empty");
                            throw new AbortException();
                        }

                        Pattern pattern = compilePattern(logger, textFinder);

                        boolean foundText = false;

                        for (String file : files) {
                            File f = new File(ws, file);

                            if (!f.exists()) {
                                logger.println("Jenkins Text Finder: Unable to" +
                                        " find file '" + f + "'");
                                continue;
                            }
                            if (!f.canRead()) {
                                logger.println("Jenkins Text Finder: Unable to" +
                                        " read from file '" + f + "'");
                                continue;
                            }

                            foundText |= checkFile(f, pattern, logger, false);
                        }

                        return foundText;
                    }
                });
            }

            if (foundText != textFinder.succeedIfFound) {
                final Result finalResult;
                if (textFinder.notBuiltIfFound) {
                    finalResult = Result.NOT_BUILT;
                } else {
                    finalResult = textFinder.unstableIfFound ? Result.UNSTABLE : Result.FAILURE;
                }
                build.setResult(finalResult);
            }
        } catch (AbortException e) {
            // no test file found
            build.setResult(Result.UNSTABLE);
        }
    }

    /**
     * Search the given regexp pattern in the file.
     *
     * @param abortAfterFirstHit true to return immediately as soon as the first hit is found. this is necessary
     *                           when we are scanning the console output, because otherwise we'll loop forever.
     */
    private boolean checkFile(File f, Pattern pattern, PrintStream logger, boolean abortAfterFirstHit) {
        boolean logFilename = true;
        boolean foundText = false;
        BufferedReader reader = null;
        try {
            // Assume default encoding and text files
            String line;
            reader = new BufferedReader(new FileReader(f));
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    if (logFilename) {// first occurrence
                        logger.println(f + ":");
                        logFilename = false;
                    }
                    logger.println(line);
                    foundText = true;
                    if (abortAfterFirstHit) { return true; }
                }
            }
        } catch (IOException e) {
            logger.println("Jenkins Text Finder: Error reading" +
                    " file '" + f + "' -- ignoring");
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return foundText;
    }

    private Pattern compilePattern(PrintStream logger, TextFinderModel textFinder) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(textFinder.regexp);
        } catch (PatternSyntaxException e) {
            logger.println("Jenkins Text Finder: Unable to compile"
                    + "regular expression '" + textFinder.regexp + "'");
            throw new AbortException();
        }
        return pattern;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public String getDisplayName() {
            return Messages.DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/text-finder/help.html";
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public List<TextFinderModel.DescriptorImpl> getItemDescriptors() {
            return Jenkins.getInstance().getDescriptorList(TextFinderModel.class);
        }

        /**
         * Checks the regular expression validity.
         */
        public FormValidation doCheckRegexp(@QueryParameter String value) throws IOException, ServletException {
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

    private static final long serialVersionUID = 1L;
}

