
package hudson.plugins.textfinder;

import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.Util;
import static hudson.Util.fixEmpty;
import hudson.util.FormFieldValidator;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Publisher;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Text Finder plugin for Hudson. Search in the workspace using a regular 
 * expression and determine build outcome based on matches. 
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class TextFinderPublisher extends Publisher implements Serializable {
    
    public final String fileSet;
    public final String regexp;
    public final boolean succeedIfFound;
    /**
     * True to also scan the whole console output
     */
    public final boolean alsoCheckConsoleOutput;

    /**
     * @stapler-constructor
     */
    public TextFinderPublisher(String fileSet, String regexp, boolean succeedIfFound, boolean alsoCheckConsoleOutput) {
        this.fileSet = Util.fixEmpty(fileSet.trim());
        this.regexp = regexp;
        this.succeedIfFound = succeedIfFound;
        this.alsoCheckConsoleOutput = alsoCheckConsoleOutput;
        
        // Attempt to compile regular expression
        try {
            Pattern.compile(regexp);
        } catch (PatternSyntaxException e) {
            // falls through 
        }
    }
    
    public boolean perform(Build build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        findText(build, listener.getLogger());
        return true;
    }

    /**
     * Indicates an orderly abortion of the processing.
     */
    private static final class AbortException extends RuntimeException {
    }

    private void findText(Build build, PrintStream logger) throws IOException, InterruptedException {
        try {
            boolean foundText = false;

            if(alsoCheckConsoleOutput) {
                logger.println("Checking console output");
                foundText |= checkFile(build.getLogFile(), compilePattern(logger), logger, true);
            } else {
                // printing this when checking console output will cause the plugin
                // to find this line, which would be pointless.
                // doing this only when fileSet!=null to avoid
                logger.println("Checking " + regexp);
            }

            final RemoteOutputStream ros = new RemoteOutputStream(logger);

            if(fileSet!=null) {
                foundText |= build.getProject().getWorkspace().act(new FileCallable<Boolean>() {
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
                            logger.println("Hudson Text Finder: File set '" +
                                    fileSet + "' is empty");
                            throw new AbortException();
                        }

                        Pattern pattern = compilePattern(logger);

                        boolean foundText = false;

                        for (String file : files) {
                            File f = new File(ws, file);

                            if (!f.exists()) {
                                logger.println("Hudson Text Finder: Unable to" +
                                    " find file '" + f + "'");
                                continue;
                            }
                            if (!f.canRead()) {
                                logger.println("Hudson Text Finder: Unable to" +
                                    " read from file '" + f + "'");
                                continue;
                            }

                            foundText |= checkFile(f, pattern, logger, false);
                        }

                        return foundText;
                    }
                });
            }

            if (foundText != succeedIfFound)
                build.setResult(Result.FAILURE);
        } catch (AbortException e) {
            // no test file found
            build.setResult(Result.UNSTABLE);
        }
    }

    /**
     * Search the given regexp pattern in the file.
     *
     * @param abortAfterFirstHit
     *      true to return immediately as soon as the first hit is found. this is necessary
     *      when we are scanning the console output, because otherwise we'll loop forever. 
     */
    private boolean checkFile(File f, Pattern pattern, PrintStream logger, boolean abortAfterFirstHit) {
        boolean logFilename = true;
        boolean foundText = false;
        try {
            // Assume default encoding and text files
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(f));
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    if (logFilename) {// first occurrence
                        logger.println(f + ":");
                        logFilename = false;
                    }
                    logger.println(line);
                    foundText = true;
                    if(abortAfterFirstHit)
                        return true;
                }
            }
        } catch (IOException e) {
            logger.println("Hudson Text Finder: Error reading" +
                " file '" + f + "' -- ignoring");
        }
        return foundText;
    }

    private Pattern compilePattern(PrintStream logger) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(regexp);
        } catch (PatternSyntaxException e) {
            logger.println("Hudson Text Finder: Unable to compile"
                    + "regular expression '" + regexp + "'");
            throw new AbortException();
        }
        return pattern;
    }

    public Descriptor<Publisher> getDescriptor() {
        return DescriptorImpl.DESCRIPTOR;
    }
    
    public static final class DescriptorImpl extends Descriptor<Publisher> {
        public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

        private DescriptorImpl() {
            super(TextFinderPublisher.class);
        }
        
        public String getDisplayName() {
            return "Hudson Text Finder";
        }

        public String getHelpFile() {
            return "/plugin/text-finder/help.html";
        }

        /**
         * Checks the regular expression validity.
         */
        public void doCheckRegexp(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            new FormFieldValidator(req,rsp,true) {
                protected void check() throws IOException, ServletException {
                    String value = fixEmpty(request.getParameter("value"));
                    if(value==null) {
                        ok(); // not entered yet
                        return;
                    }

                    try {
                        Pattern.compile(value);
                        ok();
                    } catch (PatternSyntaxException e) {
                        error(e.getMessage());
                    }
                }
            }.process();
        }

        public TextFinderPublisher newInstance(StaplerRequest req) throws FormException {
            return req.bindParameters(TextFinderPublisher.class,"textfinder_");
        }
    }

    private static final long serialVersionUID = 1L;
}
