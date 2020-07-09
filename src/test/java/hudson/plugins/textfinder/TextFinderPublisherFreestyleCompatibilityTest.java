package hudson.plugins.textfinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.textfinder.test.TestEchoBuilder;
import hudson.plugins.textfinder.test.TestWriteFileBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import java.io.File;

@SuppressWarnings("deprecation")
public class TextFinderPublisherFreestyleCompatibilityTest {

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
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        project.getPublishersList().add(textFinder);
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
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        textFinder.setUnstableIfFound(true);
        project.getPublishersList().add(textFinder);
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
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        textFinder.setNotBuiltIfFound(true);
        project.getPublishersList().add(textFinder);
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
    public void successIfNotFoundInFile() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestWriteFileBuilder(TestUtils.FILE_SET, "foobaz"));
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        project.getPublishersList().add(textFinder);
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
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        textFinder.setSucceedIfFound(true);
        project.getPublishersList().add(textFinder);
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
    public void successIfFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.UNIQUE_TEXT));
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setSucceedIfFound(true);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
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
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
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
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setUnstableIfFound(true);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
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
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setNotBuiltIfFound(true);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.NOT_BUILT, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.",
                build);
        rule.assertLogContains("Setting build result to 'NOT_BUILT'.", build);
    }

    @Test
    public void successIfNotFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.",
                build);
    }

    @Test
    public void failureIfNotFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        textFinder.setSucceedIfFound(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.",
                build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
    }

    @LocalData
    @Test
    public void persistedConfigurationBeforeMultipleFinders() {
        // Local data created using Text Finder 1.12 with the following code:
        /*
        FreeStyleProject project = rule.createFreeStyleProject();
        TextFinderPublisher textFinder = new TextFinderPublisher("foobar");
        textFinder.setFileSet("out.txt");
        textFinder.setUnstableIfFound(true);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        */
        FreeStyleProject project = rule.jenkins.getItemByFullName("test0", FreeStyleProject.class);
        assertEquals(1, project.getPublishersList().size());
        TextFinderPublisher textFinderPublisher =
                (TextFinderPublisher) project.getPublishersList().get(0);
        TextFinder textFinder = textFinderPublisher.getTextFinders().get(0);
        assertEquals("foobar", textFinder.getRegexp());
        assertEquals("out.txt", textFinder.getFileSet());
        assertEquals(Result.UNSTABLE.toString(), textFinder.getBuildResult());
        assertTrue(textFinder.isAlsoCheckConsoleOutput());
    }
}
