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
import java.util.Arrays;
import java.util.Collections;

public class TextFinderPublisherFreestyleTest {

    @Rule public JenkinsRule rule = new JenkinsRule();

    @Test
    public void successIfFoundInFile() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList()
                .add(new TestWriteFileBuilder(TestUtils.FILE_SET, TestUtils.UNIQUE_TEXT));
        TextFinder textFinder = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        textFinder.setBuildResult(Result.SUCCESS.toString());
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Collections.singletonList(textFinder));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains(
                "[Text Finder] Searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        TestUtils.assertFileContainsMatch(
                new File(build.getWorkspace().getRemote(), TestUtils.FILE_SET),
                TestUtils.UNIQUE_TEXT,
                rule,
                build);
        rule.assertLogContains(
                "[Text Finder] Finished searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
    }

    @Test
    public void failureIfFoundInFile() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList()
                .add(new TestWriteFileBuilder(TestUtils.FILE_SET, TestUtils.UNIQUE_TEXT));
        TextFinder textFinder = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Collections.singletonList(textFinder));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains(
                "[Text Finder] Searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        TestUtils.assertFileContainsMatch(
                new File(build.getWorkspace().getRemote(), TestUtils.FILE_SET),
                TestUtils.UNIQUE_TEXT,
                rule,
                build);
        rule.assertLogContains(
                "[Text Finder] Finished searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
    }

    @Test
    public void unstableIfFoundInFile() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList()
                .add(new TestWriteFileBuilder(TestUtils.FILE_SET, TestUtils.UNIQUE_TEXT));
        TextFinder textFinder = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        textFinder.setBuildResult(Result.UNSTABLE.toString());
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Collections.singletonList(textFinder));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.UNSTABLE, project);
        rule.assertLogContains(
                "[Text Finder] Searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        TestUtils.assertFileContainsMatch(
                new File(build.getWorkspace().getRemote(), TestUtils.FILE_SET),
                TestUtils.UNIQUE_TEXT,
                rule,
                build);
        rule.assertLogContains(
                "[Text Finder] Finished searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        rule.assertLogContains("Setting build result to 'UNSTABLE'.", build);
    }

    @Test
    public void notBuiltIfFoundInFile() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList()
                .add(new TestWriteFileBuilder(TestUtils.FILE_SET, TestUtils.UNIQUE_TEXT));
        TextFinder textFinder = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        textFinder.setBuildResult(Result.NOT_BUILT.toString());
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Collections.singletonList(textFinder));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.NOT_BUILT, project);
        rule.assertLogContains(
                "[Text Finder] Searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        TestUtils.assertFileContainsMatch(
                new File(build.getWorkspace().getRemote(), TestUtils.FILE_SET),
                TestUtils.UNIQUE_TEXT,
                rule,
                build);
        rule.assertLogContains(
                "[Text Finder] Finished searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        rule.assertLogContains("Setting build result to 'NOT_BUILT'.", build);
    }

    @Test
    public void abortedIfFoundInFile() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList()
                .add(new TestWriteFileBuilder(TestUtils.FILE_SET, TestUtils.UNIQUE_TEXT));
        TextFinder textFinder = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        textFinder.setBuildResult(Result.ABORTED.toString());
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Collections.singletonList(textFinder));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.ABORTED, project);
        rule.assertLogContains(
                "[Text Finder] Searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        TestUtils.assertFileContainsMatch(
                new File(build.getWorkspace().getRemote(), TestUtils.FILE_SET),
                TestUtils.UNIQUE_TEXT,
                rule,
                build);
        rule.assertLogContains(
                "[Text Finder] Finished searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        rule.assertLogContains("Setting build result to 'ABORTED'.", build);
    }

    @Test
    public void successIfNotFoundInFile() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestWriteFileBuilder(TestUtils.FILE_SET, "foobaz"));
        TextFinder textFinder = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Collections.singletonList(textFinder));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains(
                "[Text Finder] Searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        rule.assertLogContains(
                "[Text Finder] Finished searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
    }

    @Test
    public void failureIfNotFoundInFile() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestWriteFileBuilder(TestUtils.FILE_SET, "foobaz"));
        TextFinder textFinder = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        textFinder.setChangeCondition(TextFinderChangeCondition.MATCH_NOT_FOUND);
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Collections.singletonList(textFinder));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains(
                "[Text Finder] Searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        rule.assertLogContains(
                "[Text Finder] Finished searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
    }

    @Test
    public void multipleTextFindersInFile() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList()
                .add(new TestWriteFileBuilder(TestUtils.FILE_SET, TestUtils.UNIQUE_TEXT));
        TextFinder tf1 = new TextFinder(TestUtils.UNIQUE_TEXT);
        tf1.setFileSet(TestUtils.FILE_SET);
        tf1.setBuildResult(Result.SUCCESS.toString());
        TextFinder tf2 = new TextFinder(TestUtils.UNIQUE_TEXT);
        tf2.setFileSet(TestUtils.FILE_SET);
        TextFinder tf3 = new TextFinder(TestUtils.UNIQUE_TEXT);
        tf3.setFileSet(TestUtils.FILE_SET);
        tf3.setBuildResult(Result.UNSTABLE.toString());
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Arrays.asList(tf1, tf2, tf3));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains(
                "[Text Finder] Searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        TestUtils.assertFileContainsMatch(
                new File(build.getWorkspace().getRemote(), TestUtils.FILE_SET),
                TestUtils.UNIQUE_TEXT,
                rule,
                build);
        rule.assertLogContains(
                "[Text Finder] Finished searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
        rule.assertLogContains("Setting build result to 'UNSTABLE'.", build);
    }

    @Test
    public void successIfFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.UNIQUE_TEXT));
        TextFinder textFinder = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder.setBuildResult(Result.SUCCESS.toString());
        textFinder.setAlsoCheckConsoleOutput(true);
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Collections.singletonList(textFinder));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.",
                build);
    }

    @Test
    public void failureIfFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.UNIQUE_TEXT));
        TextFinder textFinder = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Collections.singletonList(textFinder));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.",
                build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
    }

    @Test
    public void unstableIfFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.UNIQUE_TEXT));
        TextFinder textFinder = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder.setBuildResult(Result.UNSTABLE.toString());
        textFinder.setAlsoCheckConsoleOutput(true);
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Collections.singletonList(textFinder));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.UNSTABLE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.",
                build);
        rule.assertLogContains("Setting build result to 'UNSTABLE'.", build);
    }

    @Test
    public void notBuiltIfFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.UNIQUE_TEXT));
        TextFinder textFinder = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder.setBuildResult(Result.NOT_BUILT.toString());
        textFinder.setAlsoCheckConsoleOutput(true);
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Collections.singletonList(textFinder));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.NOT_BUILT, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.",
                build);
        rule.assertLogContains("Setting build result to 'NOT_BUILT'.", build);
    }

    @Test
    public void abortedIfFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.UNIQUE_TEXT));
        TextFinder textFinder = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder.setBuildResult(Result.ABORTED.toString());
        textFinder.setAlsoCheckConsoleOutput(true);
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Collections.singletonList(textFinder));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.ABORTED, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.",
                build);
        rule.assertLogContains("Setting build result to 'ABORTED'.", build);
    }

    @Test
    public void successIfNotFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        TextFinder textFinder = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Collections.singletonList(textFinder));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.",
                build);
    }

    @Test
    public void failureIfNotFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        TextFinder textFinder = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        textFinder.setChangeCondition(TextFinderChangeCondition.MATCH_NOT_FOUND);
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Collections.singletonList(textFinder));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.",
                build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
    }

    @Test
    public void multipleTextFindersInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.UNIQUE_TEXT));
        TextFinder tf1 = new TextFinder(TestUtils.UNIQUE_TEXT);
        tf1.setAlsoCheckConsoleOutput(true);
        tf1.setBuildResult(Result.SUCCESS.toString());
        TextFinder tf2 = new TextFinder(TestUtils.UNIQUE_TEXT);
        tf2.setAlsoCheckConsoleOutput(true);
        TextFinder tf3 = new TextFinder(TestUtils.UNIQUE_TEXT);
        tf3.setAlsoCheckConsoleOutput(true);
        tf3.setBuildResult(Result.UNSTABLE.toString());
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher();
        textFinderPublisher.setTextFinders(Arrays.asList(tf1, tf2, tf3));
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.",
                build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
        rule.assertLogContains("Setting build result to 'UNSTABLE'.", build);
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
        page.getAnchorByText(Messages.TextFinderPublisher_DisplayName()).click();

        // Wait for the YUI JavaScript to load.
        WebClientUtil.waitForJSExec(page.getWebClient());

        // Configure the Text Finder.
        config.getInputByName("_.fileSet").setValueAttribute("file1");
        config.getInputByName("_.regexp").setValueAttribute(TestUtils.UNIQUE_TEXT);
        config.getSelectByName("_.buildResult")
                .setSelectedAttribute(Result.UNSTABLE.toString(), true);
        config.getInputByName("_.alsoCheckConsoleOutput").click();

        // Submit the page.
        rule.submit(config);

        // Ensure that the Text Finder was configured correctly.
        assertEquals(1, project.getPublishersList().size());
        TextFinderPublisher textFinderPublisher =
                (TextFinderPublisher) project.getPublishersList().get(0);
        TextFinder textFinder = textFinderPublisher.getTextFinders().get(0);
        assertEquals("file1", textFinder.getFileSet());
        assertEquals(TestUtils.UNIQUE_TEXT, textFinder.getRegexp());
        assertEquals(Result.UNSTABLE.toString(), textFinder.getBuildResult());
        assertTrue(textFinder.isAlsoCheckConsoleOutput());
    }
}
