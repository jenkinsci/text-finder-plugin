package hudson.plugins.textfinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.gargoylesoftware.htmlunit.WebClientUtil;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.textfinder.test.TestEchoBuilder;
import hudson.plugins.textfinder.test.TestWriteFileBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;

public class TextFinderPublisherFreestyleTest {

    @Rule public JenkinsRule rule = new JenkinsRule();

    @Test
    public void successIfFoundInFile() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList()
                .add(new TestWriteFileBuilder(TestUtils.FILE_SET, TestUtils.UNIQUE_TEXT));
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        textFinder.setSucceedIfFound(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains(
                "[Text Finder] Looking for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in the files at "
                        + "'"
                        + TestUtils.FILE_SET
                        + "'",
                build);
        TestUtils.assertFileContainsMatch(
                new File(build.getWorkspace().getRemote(), TestUtils.FILE_SET),
                TestUtils.UNIQUE_TEXT,
                rule,
                build);
    }

    @Test
    public void failureIfFoundInFile() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList()
                .add(new TestWriteFileBuilder(TestUtils.FILE_SET, TestUtils.UNIQUE_TEXT));
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains(
                "[Text Finder] Looking for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in the files at "
                        + "'"
                        + TestUtils.FILE_SET
                        + "'",
                build);
        TestUtils.assertFileContainsMatch(
                new File(build.getWorkspace().getRemote(), TestUtils.FILE_SET),
                TestUtils.UNIQUE_TEXT,
                rule,
                build);
    }

    @Test
    public void unstableIfFoundInFile() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList()
                .add(new TestWriteFileBuilder(TestUtils.FILE_SET, TestUtils.UNIQUE_TEXT));
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        textFinder.setUnstableIfFound(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.UNSTABLE, project);
        rule.assertLogContains(
                "[Text Finder] Looking for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in the files at "
                        + "'"
                        + TestUtils.FILE_SET
                        + "'",
                build);
        TestUtils.assertFileContainsMatch(
                new File(build.getWorkspace().getRemote(), TestUtils.FILE_SET),
                TestUtils.UNIQUE_TEXT,
                rule,
                build);
    }

    @Test
    public void notBuiltIfFoundInFile() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList()
                .add(new TestWriteFileBuilder(TestUtils.FILE_SET, TestUtils.UNIQUE_TEXT));
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        textFinder.setNotBuiltIfFound(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.NOT_BUILT, project);
        rule.assertLogContains(
                "[Text Finder] Looking for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in the files at "
                        + "'"
                        + TestUtils.FILE_SET
                        + "'",
                build);
        TestUtils.assertFileContainsMatch(
                new File(build.getWorkspace().getRemote(), TestUtils.FILE_SET),
                TestUtils.UNIQUE_TEXT,
                rule,
                build);
    }

    @Test
    public void notFoundInFile() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestWriteFileBuilder(TestUtils.FILE_SET, "foobaz"));
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains(
                "[Text Finder] Looking for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in the files at "
                        + "'"
                        + TestUtils.FILE_SET
                        + "'",
                build);
    }

    @Test
    public void successIfFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.UNIQUE_TEXT));
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setSucceedIfFound(true);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in the console output",
                build);
    }

    @Test
    public void failureIfFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.UNIQUE_TEXT));
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in the console output",
                build);
    }

    @Test
    public void unstableIfFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.UNIQUE_TEXT));
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setUnstableIfFound(true);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.UNSTABLE, project);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in the console output",
                build);
    }

    @Test
    public void notFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in the console output",
                build);
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
        config.getInputByName("_.regexp").setValueAttribute(TestUtils.UNIQUE_TEXT);
        config.getInputByName("_.unstableIfFound").click();
        config.getInputByName("_.alsoCheckConsoleOutput").click();

        // Submit the page.
        rule.submit(config);

        // Ensure that the Text Finder was configured correctly.
        assertEquals(1, project.getPublishersList().size());
        TextFinderPublisher textFinder = (TextFinderPublisher) project.getPublishersList().get(0);
        assertEquals("file1", textFinder.fileSet);
        assertEquals(TestUtils.UNIQUE_TEXT, textFinder.regexp);
        assertTrue(textFinder.unstableIfFound);
        assertTrue(textFinder.alsoCheckConsoleOutput);
    }
}
