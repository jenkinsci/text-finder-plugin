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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.servlet.ServletException;
import jenkins.MasterToSlaveFileCallable;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

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
                logger.println("Checking console output");
                foundText |=
                        checkFile(run.getLogFile(), compilePattern(logger, regexp), logger, true);
            } else {
                // printing this when checking console output will cause the plugin
                // to find this line, which would be pointless.
                // doing this only when fileSet!=null to avoid
                logger.println("Checking for " + "'" + regexp + "'" + " in " + build.getLogFile());
            }

            final RemoteOutputStream ros = new RemoteOutputStream(logger);

            if (fileSet != null) {
                foundText |= workspace.act(new FileChecker(ros, fileSet, regexp));
            }

            if (foundText != succeedIfFound) {
                run.setResult(unstableIfFound ? Result.UNSTABLE : Result.FAILURE);
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
            File f, Pattern pattern, PrintStream logger, boolean abortAfterFirstHit) {
        boolean logFilename = true;
        boolean foundText = false;
        BufferedReader reader = null;
        try {
            // Assume default encoding and text files
            String line;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
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
            logger.println("Jenkins Text Finder: Error reading file '" + f + "' -- ignoring");
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
            logger.println(
                    "Jenkins Text Finder: Unable to compile regular expression '" + regexp + "'");
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
            PrintStream logger = new PrintStream(ros, false, "UTF-8");

            // Collect list of files for searching
            FileSet fs = new FileSet();
            org.apache.tools.ant.Project p = new org.apache.tools.ant.Project();
            fs.setProject(p);
            fs.setDir(ws);
            fs.setIncludes(fileSet);
            DirectoryScanner ds = fs.getDirectoryScanner(p);

            // Any files in the final set?
            String[] files = ds.getIncludedFiles();
            if (files.length == 0) {
                logger.println("Jenkins Text Finder: File set '" + fileSet + "' is empty");
                throw new AbortException();
            }

            Pattern pattern = compilePattern(logger, regexp);

            boolean foundText = false;

            for (String file : files) {
                File f = new File(ws, file);

                if (!f.exists()) {
                    logger.println("Jenkins Text Finder: Unable to find file '" + f + "'");
                    continue;
                }
                if (!f.canRead()) {
                    logger.println("Jenkins Text Finder: Unable to read from file '" + f + "'");
                    continue;
                }

                foundText |= checkFile(f, pattern, logger, false);
            }

            return foundText;
        }
    }

    private static final long serialVersionUID = 1L;
}
