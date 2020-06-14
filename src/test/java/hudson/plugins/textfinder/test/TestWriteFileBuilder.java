package hudson.plugins.textfinder.test;

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

import javax.annotation.Nonnull;

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
            @Nonnull Run<?, ?> run,
            @Nonnull FilePath workspace,
            @Nonnull Launcher launcher,
            @Nonnull TaskListener listener)
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
