package hudson.plugins.textfinder.test;

import hudson.Launcher;
import hudson.model.*;
import hudson.plugins.textfinder.TestUtils;
import hudson.tasks.Builder;

import org.jvnet.hudson.test.TestBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

/** A test {@link Builder} that merely prints the message given to it with a prefix. */
public class TestEchoBuilder extends TestBuilder {

    private final String message;

    @DataBoundConstructor
    public TestEchoBuilder(String message) {
        this.message = TestUtils.PREFIX + message;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        listener.getLogger().println(message);
        return true;
    }
}
