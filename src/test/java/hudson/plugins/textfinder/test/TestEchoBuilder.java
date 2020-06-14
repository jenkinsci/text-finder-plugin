package hudson.plugins.textfinder.test;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.plugins.textfinder.TestUtils;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

import jenkins.tasks.SimpleBuildStep;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/** A test {@link Builder} that merely prints the message given to it with a prefix. */
public class TestEchoBuilder extends Builder implements SimpleBuildStep {

    private final String message;

    @DataBoundConstructor
    public TestEchoBuilder(String message) {
        this.message = TestUtils.PREFIX + message;
    }

    @Override
    public void perform(
            @Nonnull Run<?, ?> run,
            @Nonnull FilePath workspace,
            @Nonnull Launcher launcher,
            @Nonnull TaskListener listener) {
        listener.getLogger().println(message);
    }

    @Symbol("testEchoBuilder")
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
