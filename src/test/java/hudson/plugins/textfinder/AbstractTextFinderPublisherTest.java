package hudson.plugins.textfinder;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FreeStyleBuild.class)
public abstract class AbstractTextFinderPublisherTest {

    protected static String BUILD_DIRECTORY = ".\\target\\test-classes";
    protected static String UNSTABLE_DESCRIPTION = "Unstable when finding 'UNSTABLE'";
    protected static String UNSTABLE_REGEX_STRING = "UNSTABLE";
    protected static String ERROR_DESCRIPTION = "Failure when finding 'ERROR'";
    protected static String ERROR_REGEX_STRING = "ERROR";
    protected static final String FILE_ERROR_THEN_UNSTABLE = "ErrorThenUnstable.txt";

    protected AbstractBuild<?, ?> build;
    protected TextFinderPublisher textFinderPublisher;

    @Test
    public void test() throws Exception {
        textFinderPublisher = new TextFinderPublisher(givenAListOfTextFinderParameters());
        whenTextFinderPublisherInvoked();
        verify(build).setResult(thenExpect());
    }

    private void whenTextFinderPublisherInvoked() throws Exception {
        build = mock(FreeStyleBuild.class);
        when(build.getWorkspace()).thenReturn(new FilePath(new File(BUILD_DIRECTORY)));
        final BuildListener listener = mock(BuildListener.class);
        final PrintStream printStream = mock(PrintStream.class);
        final Launcher launcher = mock(Launcher.class);
        when(listener.getLogger()).thenReturn(printStream);
        textFinderPublisher.perform(build, launcher, listener);
    }

    protected abstract List<TextFinderParameters> givenAListOfTextFinderParameters();

    protected abstract Result thenExpect();
}