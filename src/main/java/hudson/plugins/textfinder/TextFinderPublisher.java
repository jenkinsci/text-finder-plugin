package hudson.plugins.textfinder;

import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.Extension;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
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

import jenkins.MasterToSlaveFileCallable;
import jenkins.tasks.SimpleBuildStep;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    @NonNull private List<TextFinder> textFinders;

    @Deprecated
    @Restricted(NoExternalUse.class)
    public transient String fileSet;

    @Deprecated
    @Restricted(NoExternalUse.class)
    public transient String regexp;

    @Deprecated
    @Restricted(NoExternalUse.class)
    public transient boolean succeedIfFound;

    @Deprecated
    @Restricted(NoExternalUse.class)
    public transient boolean unstableIfFound;

    @Deprecated
    @Restricted(NoExternalUse.class)
    public transient boolean notBuiltIfFound;

    /** True to also scan the whole console output */
    @Deprecated
    @Restricted(NoExternalUse.class)
    public transient boolean alsoCheckConsoleOutput;

    /** Used only by Stapler in the snippetizer. */
    @Deprecated
    @Restricted(DoNotUse.class)
    public transient String buildResult;

    @DataBoundConstructor
    public TextFinderPublisher() {
        textFinders = new ArrayList<>();
    }

    @Deprecated
    @Restricted(NoExternalUse.class)
    public TextFinderPublisher(String regexp) {
        this();
        textFinders.add(new TextFinder(regexp));
    }

    @Deprecated
    @Restricted(NoExternalUse.class)
    public TextFinderPublisher(
            String fileSet,
            String regexp,
            boolean succeedIfFound,
            boolean unstableIfFound,
            boolean alsoCheckConsoleOutput) {
        this(regexp);
        setFileSet(fileSet);
        setSucceedIfFound(succeedIfFound);
        setUnstableIfFound(unstableIfFound);
        setAlsoCheckConsoleOutput(alsoCheckConsoleOutput);
    }

    @NonNull
    public List<TextFinder> getTextFinders() {
        return textFinders;
    }

    @DataBoundSetter
    public void setTextFinders(List<TextFinder> textFinders) {
        this.textFinders = textFinders != null ? new ArrayList<>(textFinders) : new ArrayList<>();
    }

    @DataBoundSetter
    @Deprecated
    @Restricted(NoExternalUse.class)
    public void setRegexp(String regexp) {
        getFirst().setRegexp(regexp);
    }

    @DataBoundSetter
    @Deprecated
    @Restricted(NoExternalUse.class)
    public void setFileSet(String fileSet) {
        getFirst().setFileSet(fileSet);
    }

    @DataBoundSetter
    @Deprecated
    @Restricted(NoExternalUse.class)
    public void setBuildResult(String buildResult) {
        getFirst().setBuildResult(buildResult);
    }

    @DataBoundSetter
    @Deprecated
    @Restricted(NoExternalUse.class)
    public void setSucceedIfFound(boolean succeedIfFound) {
        if (succeedIfFound) {
            getFirst().setChangeCondition(TextFinderChangeCondition.MATCH_NOT_FOUND);
        }
    }

    @DataBoundSetter
    @Deprecated
    @Restricted(NoExternalUse.class)
    public void setUnstableIfFound(boolean unstableIfFound) {
        // Versions prior to 1.13 treated NOT_BUILT with higher precedence than UNSTABLE. For
        // compatibility, maintain the same behavior when migrating settings from these old
        // versions.
        if (unstableIfFound && !getFirst().getBuildResult().equals(Result.NOT_BUILT.toString())) {
            getFirst().setBuildResult(Result.UNSTABLE.toString());
        }
    }

    @DataBoundSetter
    @Deprecated
    @Restricted(NoExternalUse.class)
    public void setNotBuiltIfFound(boolean notBuiltIfFound) {
        if (notBuiltIfFound) {
            getFirst().setBuildResult(Result.NOT_BUILT.toString());
        }
    }

    @DataBoundSetter
    @Deprecated
    @Restricted(NoExternalUse.class)
    public void setAlsoCheckConsoleOutput(boolean alsoCheckConsoleOutput) {
        getFirst().setAlsoCheckConsoleOutput(alsoCheckConsoleOutput);
    }

    private TextFinder getFirst() {
        if (textFinders.isEmpty()) {
            // This is gross, but fortunately it is only used in a deprecated code path.
            TextFinder first = new TextFinder("");
            textFinders.add(first);
            return first;
        } else {
            return textFinders.get(0);
        }
    }

    /**
     * Called by XStream after object construction
     *
     * @return modified object
     */
    protected Object readResolve() {
        if (regexp != null) {
            setTextFinders(Collections.singletonList(new TextFinder(regexp)));
            regexp = null;
        }

        if (fileSet != null) {
            setFileSet(fileSet);
            fileSet = null;
        }

        if (succeedIfFound) {
            setSucceedIfFound(succeedIfFound);
            succeedIfFound = false;
        }

        if (unstableIfFound) {
            setUnstableIfFound(unstableIfFound);
            unstableIfFound = false;
        }

        if (notBuiltIfFound) {
            setNotBuiltIfFound(notBuiltIfFound);
            notBuiltIfFound = false;
        }

        if (alsoCheckConsoleOutput) {
            setAlsoCheckConsoleOutput(alsoCheckConsoleOutput);
            alsoCheckConsoleOutput = false;
        }

        return this;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public void perform(
            @NonNull Run<?, ?> run,
            @NonNull FilePath workspace,
            @NonNull Launcher launcher,
            @NonNull TaskListener listener)
            throws InterruptedException, IOException {
        for (TextFinder textFinder : textFinders) {
            findText(textFinder, run, workspace, listener);
        }
    }

    /** Indicates an orderly abortion of the processing. */
    private static final class AbortException extends RuntimeException {}

    private static void findText(
            TextFinder textFinder, Run<?, ?> run, FilePath workspace, TaskListener listener)
            throws IOException, InterruptedException {
        try {
            PrintStream logger = listener.getLogger();
            boolean foundText = false;

            if (textFinder.isAlsoCheckConsoleOutput()) {
                // Do not mention the pattern we are looking for to avoid false positives
                logger.println("[Text Finder] Searching console output...");
                foundText |=
                        checkConsole(run, compilePattern(logger, textFinder.getRegexp()), logger);
                logger.println(
                        "[Text Finder] Finished searching for pattern '"
                                + textFinder.getRegexp()
                                + "' in console output.");
            }

            if (textFinder.getFileSet() != null) {
                logger.println(
                        "[Text Finder] Searching for pattern '"
                                + textFinder.getRegexp()
                                + "' in file set '"
                                + textFinder.getFileSet()
                                + "'...");
                RemoteOutputStream ros = new RemoteOutputStream(logger);
                foundText |=
                        workspace.act(
                                new FileChecker(
                                        ros, textFinder.getFileSet(), textFinder.getRegexp()));
                logger.println(
                        "[Text Finder] Finished searching for pattern '"
                                + textFinder.getRegexp()
                                + "' in file set '"
                                + textFinder.getFileSet()
                                + "'.");
            }

            Result result = Result.SUCCESS;
            switch (textFinder.getChangeCondition()) {
                case MATCH_FOUND:
                    if (foundText) {
                        result = Result.fromString(textFinder.getBuildResult());
                    }
                    break;
                case MATCH_NOT_FOUND:
                    if (!foundText) {
                        result = Result.fromString(textFinder.getBuildResult());
                    }
                    break;
                default:
                    throw new IllegalStateException(
                            "Unexpected value: " + textFinder.getChangeCondition());
            }

            if (!result.equals(Result.SUCCESS)) {
                logger.println("[Text Finder] Setting build result to '" + result + "'.");
                run.setResult(result);
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
