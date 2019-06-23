package hudson.plugins.textfinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.gargoylesoftware.htmlunit.WebClientUtil;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.Functions;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.BatchFile;
import hudson.tasks.CommandInterpreter;
import hudson.tasks.Shell;
import java.io.File;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class TextFinderPublisherFreestyleTest {

    private static final String UNIQUE_TEXT = "foobar";
    private static final String ECHO_UNIQUE_TEXT = "echo " + UNIQUE_TEXT;

    @Rule public JenkinsRule rule = new JenkinsRule();

    private void assertLogContainsMatch(
            File file, String text, FreeStyleBuild build, boolean isShell) throws IOException {
        String prompt;
        if (isShell) {
            prompt = Functions.isWindows() ? ">" : "+ ";
        } else {
            prompt = "";
        }
        rule.assertLogContains(
                String.format(
                        "%s:%s%s%s", file, System.getProperty("line.separator"), prompt, text),
                build);
    }

    @Test
    public void successIfFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        CommandInterpreter command =
                Functions.isWindows()
                        ? new BatchFile("prompt $G\n" + ECHO_UNIQUE_TEXT)
                        : new Shell(ECHO_UNIQUE_TEXT);
        project.getBuildersList().add(command);
        TextFinderPublisher textFinder = new TextFinderPublisher(UNIQUE_TEXT);
        textFinder.setSucceedIfFound(true);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the console output",
                build);
        assertLogContainsMatch(build.getLogFile(), ECHO_UNIQUE_TEXT, build, true);
        rule.assertBuildStatus(Result.SUCCESS, build);
    }

    @Test
    public void failureIfFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        CommandInterpreter command =
                Functions.isWindows()
                        ? new BatchFile("prompt $G\n" + ECHO_UNIQUE_TEXT)
                        : new Shell(ECHO_UNIQUE_TEXT);
        project.getBuildersList().add(command);
        TextFinderPublisher textFinder = new TextFinderPublisher(UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the console output",
                build);
        assertLogContainsMatch(build.getLogFile(), ECHO_UNIQUE_TEXT, build, true);
        rule.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    public void unstableIfFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        CommandInterpreter command =
                Functions.isWindows()
                        ? new BatchFile("prompt $G\n" + ECHO_UNIQUE_TEXT)
                        : new Shell(ECHO_UNIQUE_TEXT);
        project.getBuildersList().add(command);
        TextFinderPublisher textFinder = new TextFinderPublisher(UNIQUE_TEXT);
        textFinder.setUnstableIfFound(true);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the console output",
                build);
        assertLogContainsMatch(build.getLogFile(), ECHO_UNIQUE_TEXT, build, true);
        rule.assertBuildStatus(Result.UNSTABLE, build);
    }

    @Test
    public void notFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        TextFinderPublisher textFinder = new TextFinderPublisher(UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the console output",
                build);
        rule.assertBuildStatus(Result.SUCCESS, build);
    }

    @Test
    public void createTextFinderViaWebClient() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        assertEquals(0, project.getPublishersList().size());

        // Go to the "Configure" page.
        JenkinsRule.WebClient webClient = rule.createWebClient();
        HtmlPage page = webClient.goTo(project.getUrl() + "/configure");

        // Add a Text Finder.
        HtmlForm config = page.getFormByName("config");
        rule.getButtonByCaption(config, "Add post-build action").click();
        page.getAnchorByText("Text Finder").click();

        // Wait for the YUI JavaScript to load.
        WebClientUtil.waitForJSExec(page.getWebClient());

        // Configure the Text Finder.
        config.getInputByName("_.fileSet").setValueAttribute("file1");
        config.getInputByName("_.regexp").setValueAttribute(UNIQUE_TEXT);
        config.getInputByName("_.unstableIfFound").click();
        config.getInputByName("_.alsoCheckConsoleOutput").click();

        // Submit the page.
        rule.submit(config);

        // Ensure that the Text Finder was configured correctly.
        assertEquals(1, project.getPublishersList().size());
        TextFinderPublisher textFinder = (TextFinderPublisher) project.getPublishersList().get(0);
        assertEquals("file1", textFinder.fileSet);
        assertEquals(UNIQUE_TEXT, textFinder.regexp);
        assertTrue(textFinder.unstableIfFound);
        assertTrue(textFinder.alsoCheckConsoleOutput);
    }
}
