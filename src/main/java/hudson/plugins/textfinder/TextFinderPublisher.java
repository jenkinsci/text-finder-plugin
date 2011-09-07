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
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Text Finder plugin for Jenkins. Search in the workspace using a regular
 * expression and determine build outcome based on matches.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class TextFinderPublisher extends Recorder implements Serializable {

    List<TextFinderParameters> textFinderParameters;

    @DataBoundConstructor
    public TextFinderPublisher(List<TextFinderParameters> textFinderParameters) {
        this.textFinderParameters = textFinderParameters;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();

        logger.println("Performing TextFinder checks on filesets");

        for (TextFinderParameters parameter : textFinderParameters) {
            findText(build, logger, parameter);
        }

        return true;
    }

    /**
     * Indicates an orderly abortion of the processing.
     */
    private static final class AbortException extends RuntimeException {
    }

    private void findText(AbstractBuild build, PrintStream logger, final TextFinderParameters parameter) throws IOException, InterruptedException {

        final Pattern pattern = compilePattern(logger, parameter.getRegexp());
        final String fileSet = parameter.getFileSet();

        try {
            boolean foundText = false;

            if (parameter.isAlsoCheckConsoleOutput()) {
                logger.println("Checking console output");
                foundText |= checkFile(build.getLogFile(), pattern, logger, true, parameter);
            } else {
                // printing this when checking console output will cause the plugin
                // to find this line, which would be pointless.
                // doing this only when fileSet!=null to avoid
                logger.println("Checking " + fileSet);
            }

            final RemoteOutputStream ros = new RemoteOutputStream(logger);

            if (fileSet != null) {
                foundText |= build.getWorkspace().act(new FileCallable<Boolean>() {
                    public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
                        PrintStream logger = new PrintStream(ros);

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

                        boolean foundText = false;

                        for (String file : files) {
                            logger.println("Processing: " + file);
                            File f = new File(ws, file);

                            if (!f.exists()) {
                                logger.println("Jenkins Text Finder: Unable to find file '" + f + "'");
                                continue;
                            }
                            if (!f.canRead()) {
                                logger.println("Jenkins Text Finder: Unable to read from file '" + f + "'");
                                continue;
                            }

                            foundText |= checkFile(f, pattern, logger, false, parameter);
                        }

                        return foundText;
                    }
                });
            }

            if (foundText != parameter.isSucceedIfFound())
                build.setResult(parameter.isUnstableIfFound() ? Result.UNSTABLE : Result.FAILURE);
        } catch (AbortException e) {
            build.setResult(Result.UNSTABLE);
            logger.println("No test file found");
        }
    }

    /**
     * Search the given regexp pattern in the file.
     *
     * @param abortAfterFirstHit true to return immediately as soon as the first hit is found. this is necessary
     *                           when we are scanning the console output, because otherwise we'll loop forever.
     */
    private boolean checkFile(File f, Pattern pattern, PrintStream logger, boolean abortAfterFirstHit, TextFinderParameters parameters) {
        String encoding = parameters.getEncoding();
        boolean logFilename = true;
        boolean foundText = false;
        BufferedReader reader = null;
        try {
            String line;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding));
            } catch (UnsupportedEncodingException e) {
                logger.println("Error using encoding " + encoding + ". Using UTF-8 as default.");
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            }
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    if (logFilename) {// first occurrence
                        logger.println("");
                        logger.println("Issue found in file: " + f);
                        logFilename = false;
                    }
                    logger.println(String.format("Line %d: %s. Line is '%s'", lineNumber, parameters.getDescription(), line));
                    foundText = true;
                    if (abortAfterFirstHit)
                        return true;
                }
                lineNumber ++;
            }
        } catch (IOException e) {
            logger.println("Jenkins Text Finder: Error reading" + " file '" + f + "' -- ignoring");
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return foundText;
    }

    private Pattern compilePattern(PrintStream logger, String regularExpression) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(regularExpression);
        } catch (PatternSyntaxException e) {
            logger.println("Jenkins Text Finder: Unable to compile regular expression '" + regularExpression + "'");
            throw new AbortException();
        }
        return pattern;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            super(TextFinderPublisher.class);
        }

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

//        /**
//         * Checks the regular expression validity.
//         */
//        public FormValidation doCheckRegexp(@QueryParameter String value) throws IOException, ServletException {
//            value = fixEmpty(value);
//            if (value == null)
//                return FormValidation.ok(); // not entered yet
//
//            try {
//                Pattern.compile(value);
//                return FormValidation.ok();
//            } catch (PatternSyntaxException e) {
//                return FormValidation.error(e.getMessage());
//            }
//        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) {
            return req.bindJSON(TextFinderPublisher.class, formData);
        }
    }

    public List<TextFinderParameters> getTextFinderParameters() {
        return textFinderParameters;
    }

    public void setTextFinderParameters(List<TextFinderParameters> textFinderParameters) {
        this.textFinderParameters = textFinderParameters;
    }

    private static final long serialVersionUID = 1L;
}
