package hudson.plugins.textfinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.gargoylesoftware.htmlunit.WebClientUtil;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.Functions;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.BatchFile;
import hudson.tasks.CommandInterpreter;
import hudson.tasks.Shell;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class TextFinderPublisherFreestyleTest {

    private static final String UNIQUE_TEXT = "foobar";
    private static final String ECHO_UNIQUE_TEXT = "echo " + UNIQUE_TEXT;

    @Rule public JenkinsRule rule = new JenkinsRule();

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
        FreeStyleBuild build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the console output",
                build);
        TestUtils.assertConsoleContainsMatch(ECHO_UNIQUE_TEXT, rule, build, true);
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
        TestUtils.assertConsoleContainsMatch(ECHO_UNIQUE_TEXT, rule, build, true);
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
        TestUtils.assertConsoleContainsMatch(ECHO_UNIQUE_TEXT, rule, build, true);
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
    public void lastFinderWins() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject("freestyle");
        CommandInterpreter command =
                Functions.isWindows()
                        ? new BatchFile("prompt $G\n" + ECHO_UNIQUE_TEXT)
                        : new Shell(ECHO_UNIQUE_TEXT);
        project.getBuildersList().add(command);

        List<TextFinderModel> finders = new ArrayList<>();
        TextFinderModel t1 = new TextFinderModel(UNIQUE_TEXT); // 2nd
        t1.setAlsoCheckConsoleOutput(true);
        t1.setSucceedIfFound(true);
        TextFinderModel t2 =
                new TextFinderModel(UNIQUE_TEXT); // 3rd, this one must win; note that not_build is
        // unmodificable
        t2.setAlsoCheckConsoleOutput(true);
        t2.setUnstableIfFound(true);
        finders.add(t1);
        finders.add(t2);
        TextFinderPublisher textFinder = new TextFinderPublisher(UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        textFinder.setSucceedIfFound(true);
        textFinder.setAdditionalTextFinders(finders);
        project.getPublishersList().add(textFinder);

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the console output",
                build);
        TestUtils.assertConsoleContainsMatch(ECHO_UNIQUE_TEXT, rule, build, true);
        rule.assertBuildStatus(Result.UNSTABLE, build);
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
        assertEquals("file1", textFinder.getFileSet());
        assertEquals(UNIQUE_TEXT, textFinder.getRegexp());
        assertTrue(textFinder.isUnstableIfFound());
        assertTrue(textFinder.isAlsoCheckConsoleOutput());
    }

    @Test
    public void createMultipleTextFindersViaWebClient() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        assertEquals(0, project.getPublishersList().size());

        // Go to the "Configure" page.
        JenkinsRule.WebClient webClient = rule.createWebClient();
        HtmlPage page = webClient.goTo(project.getUrl() + "/configure");

        // Add a Text Finder.
        HtmlForm config = page.getFormByName("config");
        rule.getButtonByCaption(config, "Add post-build action").click();
        HtmlAnchor a1 = page.getAnchorByText("Text Finder");
        a1.click();

        // Wait for the YUI JavaScript to load.
        WebClientUtil.waitForJSExec(page.getWebClient());

        // Configure the Text Finder.
        HtmlInput i0 = config.getInputByName("_.fileSet");
        i0.setValueAttribute("file1");
        HtmlInput i1 = config.getInputByName("_.regexp");
        i1.setValueAttribute(UNIQUE_TEXT);
        HtmlInput i2 = config.getInputByName("_.unstableIfFound");
        i2.click();
        HtmlInput i3 = config.getInputByName("_.alsoCheckConsoleOutput");
        i3.click();
        // add another Text Finder
        List<HtmlAnchor> al1 = page.getAnchors();
        rule.getButtonByCaption(config, "Add additional Text Finder").click();
        List<HtmlAnchor> al2 = page.getAnchors();
        // no need to click to the first  "add additional textfinder"
        // we can click the invisibel one  right away
        List<HtmlElement> linksButtons =
                page.getDocumentElement().getElementsByAttribute("a", "href", "#");
        for (HtmlElement e : linksButtons) {
            String s = e.getTextContent();
            if (s.trim().equals("Text Finder")) {
                e.click();
                // Wait for the YUI JavaScript to load.
                WebClientUtil.waitForJSExec(page.getWebClient());
            }
        }
        // Configure the second Text Finder
        List<HtmlInput> ii0 = config.getInputsByName("_.fileSet");
        ii0.get(1).setValueAttribute("file2");
        List<HtmlInput> ii1 = config.getInputsByName("_.regexp");
        ii1.get(1).setValueAttribute(UNIQUE_TEXT);
        List<HtmlInput> ii2 = config.getInputsByName("_.notBuiltIfFound");
        ii2.get(1).click();
        List<HtmlInput> ii3 = config.getInputsByName("_.alsoCheckConsoleOutput");
        ii3.get(1).click();

        // Submit the page.
        rule.submit(config);

        // Ensure that the Text Finder was configured correctly.
        assertEquals(1, project.getPublishersList().size());
        TextFinderPublisher textFinder = (TextFinderPublisher) project.getPublishersList().get(0);
        assertEquals("file1", textFinder.getFileSet());
        assertEquals(UNIQUE_TEXT, textFinder.getRegexp());
        assertTrue(textFinder.isUnstableIfFound());
        assertFalse(textFinder.isNotBuiltIfFound());
        assertTrue(textFinder.isAlsoCheckConsoleOutput());
        assertEquals(1, textFinder.getAdditionalTextFinders().size());
        TextFinderModel textFinder2 = textFinder.getAdditionalTextFinders().get(0);
        assertEquals("file2", textFinder2.getFileSet());
        assertEquals(UNIQUE_TEXT, textFinder2.getRegexp());
        assertFalse(textFinder2.isUnstableIfFound());
        assertTrue(textFinder2.isAlsoCheckConsoleOutput());
        assertTrue(textFinder2.isNotBuiltIfFound());
    }
}
