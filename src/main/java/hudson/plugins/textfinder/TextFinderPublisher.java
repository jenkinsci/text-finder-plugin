
package hudson.plugins.textfinder;

import hudson.Launcher;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.VirtualChannel;
import hudson.FilePath.FileCallable;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.Publisher;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import org.kohsuke.stapler.StaplerRequest;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.DirectoryScanner;
import java.util.regex.*;

/**
 * Text Finder plugin for Hudson. Search in the workspace using a regular 
 * expression and determine build outcome based on matches. 
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class TextFinderPublisher extends Publisher {
    
    private final String fileSet;
    private final String regexp;
    private final boolean succeedIfFound;

    TextFinderPublisher(String fileSet, String regexp, boolean succeedIfFound) {
        this.fileSet = fileSet;
        this.regexp = regexp;
        this.succeedIfFound = succeedIfFound;
        
        // Attempt to compile regular expression
        try {
            Pattern.compile(regexp);
        }
        catch (PatternSyntaxException e) {
            // falls through 
        }
    }
    
    public String getFileSet() {
        return fileSet;
    }
    
    public String getRegexp() {
        return regexp;
    }
    
    public boolean getSucceedIfFound() {
        return succeedIfFound;
    }
    
    public boolean perform(Build build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        findText(build, listener.getLogger());
        return true;
    }

    /**
     * Indicates an orderly abortion of the processing.
     */
    private static final class AbortException extends IOException {
    }

    private void findText(Build build, PrintStream logger) throws IOException, InterruptedException {
        logger.println("Checking " + regexp);

        final RemoteOutputStream ros = new RemoteOutputStream(logger);

        try {
            Boolean foundText = build.getProject().getWorkspace().act(new FileCallable<Boolean>() {
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

                    Pattern pattern;
                    try {
                        pattern = Pattern.compile(regexp);
                    } catch (PatternSyntaxException e) {
                        logger.println("Hudson Text Finder: Unable to compile"
                                + "regular expression '" + regexp + "'");
                        throw new AbortException();
                    }

                    boolean foundText = false;

                    for (String file : files) {
                        File f = new File(ws, file);
                        boolean logFilename = true;

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

                        try {
                            // Assume default encoding and text files
                            String line;
                            BufferedReader reader = new BufferedReader(new FileReader(f));
                            while ((line = reader.readLine()) != null) {
                                Matcher matcher = pattern.matcher(line);
                                if (matcher.find()) {       // first occurrence
                                    if (logFilename) {
                                        logger.println(f + ":");
                                        logFilename = false;
                                    }
                                    logger.println(line);
                                    foundText = true;
                                }
                            }
                        } catch (IOException e) {
                            logger.println("Hudson Text Finder: Error reading" +
                                " file '" + f + "' -- ignoring");
                        }
                    }

                    return foundText;
                }
            });

            if (foundText != succeedIfFound)
                build.setResult(Result.FAILURE);
        } catch (AbortException e) {
            // no test file found
            build.setResult(Result.UNSTABLE);
        }
    }
    
    public Descriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }
    
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
    
    public static final class DescriptorImpl extends Descriptor<Publisher> {
        DescriptorImpl() {
            super(TextFinderPublisher.class);
        }
        
        public String getDisplayName() {
            return "Hudson Text Finder";
        }

        public String getHelpFile() {
            return "/plugin/text-finder/help.html";
        }
               
        public TextFinderPublisher newInstance(StaplerRequest req) throws FormException {
            return new TextFinderPublisher(req.getParameter("textfinder_fileset"),
                    req.getParameter("textfinder_regexp"),
                    "on".equals(req.getParameter("textfinder_succeedIfFound")));
        }
    }
}
