package hudson.plugins.textfinder;

import static hudson.Util.fixEmpty;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;

/**
 * Text Finder plugin for Jenkins. Search in the workspace using a regular 
 * expression and determine build outcome based on matches. 
 *
 * @author Santiago.PericasGeertsen@sun.com, jochen.fuerbacher@1und1.de
 */
public class TextFinderPipelinePublisher extends TextFinderPublisher implements SimpleBuildStep {
    
	@DataBoundConstructor
    public TextFinderPipelinePublisher(String regexp,
			boolean succeedIfFound, boolean unstableIfFound) {
		super(null, regexp, succeedIfFound, unstableIfFound, true);
	}
    
    @Override
	public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher,
			TaskListener listener) throws InterruptedException, IOException {
    	findText(run, listener.getLogger());		
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
    }

}

