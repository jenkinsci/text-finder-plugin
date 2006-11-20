
package hudson.plugins.textfinder;

import hudson.Launcher;
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
    
    private Pattern pattern = null;
    
    TextFinderPublisher(String fileSet, String regexp, boolean succeedIfFound) {
        this.fileSet = fileSet;
        this.regexp = regexp;
        this.succeedIfFound = succeedIfFound;
        
        // Attempt to compile regular expression
        try {
            pattern = Pattern.compile(regexp);
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
    
    public boolean perform(Build build, Launcher launcher, BuildListener listener) {
        // Do we have a pattern?
        if (pattern == null) {
            listener.getLogger().println("Hudson Text Finder: Unable to compile"
                    + "regular expression '" + regexp + "'");
            build.setResult(Result.UNSTABLE);
            return true;            
        }
        
        return findText(build, listener.getLogger());
    }
                
    private boolean findText(Build build, PrintStream logger) {        
        File ws = build.getProject().getWorkspace().getLocal();
        
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
            build.setResult(Result.UNSTABLE);
            return true;
        }
        
        boolean foundText = false;
        
        for (int i = 0; i < files.length; i++) {
            boolean logFilename = true;
            
            File f = new File(ws, files[i]);
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
            }
            catch (IOException e) {
                logger.println("Hudson Text Finder: Error reading" +
                        " file '" + f + "' -- ignoring");                
            }
        }
        
        return (foundText == succeedIfFound);
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
