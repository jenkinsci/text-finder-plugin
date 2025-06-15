package hudson.plugins.textfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.textfinder.test.TestEchoBuilder;
import hudson.plugins.textfinder.test.TestWriteFileBuilder;
import java.io.File;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;

@SuppressWarnings("deprecation")
@WithJenkins
class TextFinderPublisherFreestyleCompatibilityTest {

    @Test
    void successIfFoundInFile(JenkinsRule rule) throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestWriteFileBuilder(TestUtils.FILE_SET, TestUtils.UNIQUE_TEXT));
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
                new File(build.getWorkspace().getRemote(), TestUtils.FILE_SET), TestUtils.UNIQUE_TEXT, rule, build);
        rule.assertLogContains(
                "[Text Finder] Finished searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
    }

    @Test
    void failureIfFoundInFile(JenkinsRule rule) throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestWriteFileBuilder(TestUtils.FILE_SET, TestUtils.UNIQUE_TEXT));
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
                new File(build.getWorkspace().getRemote(), TestUtils.FILE_SET), TestUtils.UNIQUE_TEXT, rule, build);
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
    void unstableIfFoundInFile(JenkinsRule rule) throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestWriteFileBuilder(TestUtils.FILE_SET, TestUtils.UNIQUE_TEXT));
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
                new File(build.getWorkspace().getRemote(), TestUtils.FILE_SET), TestUtils.UNIQUE_TEXT, rule, build);
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
    void notBuiltIfFoundInFile(JenkinsRule rule) throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestWriteFileBuilder(TestUtils.FILE_SET, TestUtils.UNIQUE_TEXT));
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
                new File(build.getWorkspace().getRemote(), TestUtils.FILE_SET), TestUtils.UNIQUE_TEXT, rule, build);
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
    void successIfNotFoundInFile(JenkinsRule rule) throws Exception {
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
    void failureIfNotFoundInFile(JenkinsRule rule) throws Exception {
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
    void successIfFoundInConsole(JenkinsRule rule) throws Exception {
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
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
    }

    @Test
    void failureIfFoundInConsole(JenkinsRule rule) throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.UNIQUE_TEXT));
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
    }

    @Test
    void unstableIfFoundInConsole(JenkinsRule rule) throws Exception {
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
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'UNSTABLE'.", build);
    }

    @Test
    void notBuiltIfFoundInConsole(JenkinsRule rule) throws Exception {
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
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'NOT_BUILT'.", build);
    }

    @Test
    void successIfNotFoundInConsole(JenkinsRule rule) throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
    }

    @Test
    void failureIfNotFoundInConsole(JenkinsRule rule) throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        textFinder.setSucceedIfFound(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
    }

    @LocalData
    @Test
    void persistedConfigurationBeforeMultipleFinders(JenkinsRule rule) {
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

    @LocalData
    @Test
    void persistedConfigurationBeforeChangeCondition(JenkinsRule rule) {
        // Local data created using Text Finder 1.13 with the following code:
        /*
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList()
                .add(new TestWriteFileBuilder(TestUtils.FILE_SET, TestUtils.UNIQUE_TEXT));
        TextFinderPublisher textFinder = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        textFinder.setFileSet(TestUtils.FILE_SET);
        project.getPublishersList().add(textFinder);
        */
        FreeStyleProject project = rule.jenkins.getItemByFullName("test0", FreeStyleProject.class);
        assertEquals(1, project.getPublishersList().size());
        TextFinderPublisher textFinderPublisher =
                (TextFinderPublisher) project.getPublishersList().get(0);
        TextFinder textFinder = textFinderPublisher.getTextFinders().get(0);
        assertEquals(TestUtils.UNIQUE_TEXT, textFinder.getRegexp());
        assertEquals(TestUtils.FILE_SET, textFinder.getFileSet());
        assertEquals(Result.FAILURE.toString(), textFinder.getBuildResult());
        assertEquals(TextFinderChangeCondition.MATCH_FOUND, textFinder.getChangeCondition());
        assertFalse(textFinder.isAlsoCheckConsoleOutput());
    }
}
