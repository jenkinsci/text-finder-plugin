package hudson.plugins.textfinder.test;

import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

import jenkins.tasks.SimpleBuildStep;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

public class TestWriteFileBuilder extends Builder implements SimpleBuildStep {

    private final String file;
    private final String text;

    @DataBoundConstructor
    public TestWriteFileBuilder(String file, String text) {
        this.file = file;
        this.text = text;
    }

    @Override
    public void perform(
            @NonNull Run<?, ?> run,
            @NonNull FilePath workspace,
            @NonNull Launcher launcher,
            @NonNull TaskListener listener)
            throws InterruptedException, IOException {
        FilePath filePath = workspace.child(file);
        filePath.write(text, null);
    }

    @Symbol("testWriteFileBuilder")
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
