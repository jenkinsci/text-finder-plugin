package hudson.plugins.textfinder.test;

import hudson.Extension;
import hudson.plugins.textfinder.TestUtils;

import org.jenkinsci.plugins.workflow.steps.EchoStep;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.kohsuke.stapler.DataBoundConstructor;

/** A test {@link Step} that merely prints the message given to it with a prefix. */
public class TestEchoStep extends EchoStep {

    @DataBoundConstructor
    public TestEchoStep(String message) {
        super(TestUtils.PREFIX + message);
    }

    @Extension
    public static final class DescriptorImpl extends EchoStep.DescriptorImpl {

        @Override
        public String getFunctionName() {
            return "testEcho";
        }
    }
}
