package hudson.plugins.textfinder;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import java.io.File;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class TextFinderPublisherFreestyleJobDslTest {

    @Rule
    public JenkinsRule rule = new JenkinsRule();

    @Test
    public void successIfFoundInFile() throws Exception {
        FreeStyleProject project = createProjectFromDsl("  steps {\n"
                + "    testWriteFileBuilder {\n"
                + "      file '"
                + TestUtils.FILE_SET
                + "'\n"
                + "      text '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "    }\n"
                + "  }\n"
                + "  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "          fileSet '"
                + TestUtils.FILE_SET
                + "'\n"
                + "          buildResult 'SUCCESS'\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
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
    public void failureIfFoundInFile() throws Exception {
        FreeStyleProject project = createProjectFromDsl("  steps {\n"
                + "    testWriteFileBuilder {\n"
                + "      file '"
                + TestUtils.FILE_SET
                + "'\n"
                + "      text '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "    }\n"
                + "  }\n"
                + "  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      fileSet '"
                + TestUtils.FILE_SET
                + "'\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
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
    public void unstableIfFoundInFile() throws Exception {
        FreeStyleProject project = createProjectFromDsl("  steps {\n"
                + "    testWriteFileBuilder {\n"
                + "      file '"
                + TestUtils.FILE_SET
                + "'\n"
                + "      text '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "    }\n"
                + "  }\n"
                + "  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      fileSet '"
                + TestUtils.FILE_SET
                + "'\n"
                + "      buildResult 'UNSTABLE'\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
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
    public void notBuiltIfFoundInFile() throws Exception {
        FreeStyleProject project = createProjectFromDsl("  steps {\n"
                + "    testWriteFileBuilder {\n"
                + "      file '"
                + TestUtils.FILE_SET
                + "'\n"
                + "      text '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "    }\n"
                + "  }\n"
                + "  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      fileSet '"
                + TestUtils.FILE_SET
                + "'\n"
                + "      buildResult 'NOT_BUILT'\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
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
    public void abortedIfFoundInFile() throws Exception {
        FreeStyleProject project = createProjectFromDsl("  steps {\n"
                + "    testWriteFileBuilder {\n"
                + "      file '"
                + TestUtils.FILE_SET
                + "'\n"
                + "      text '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "    }\n"
                + "  }\n"
                + "  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      fileSet '"
                + TestUtils.FILE_SET
                + "'\n"
                + "      buildResult 'ABORTED'\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.ABORTED, project);
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
        rule.assertLogContains("Setting build result to 'ABORTED'.", build);
    }

    @Test
    public void multipleTextFindersInFile() throws Exception {
        FreeStyleProject project = createProjectFromDsl("  steps {\n"
                + "    testWriteFileBuilder {\n"
                + "      file '"
                + TestUtils.FILE_SET
                + "'\n"
                + "      text '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "    }\n"
                + "  }\n"
                + "  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      fileSet '"
                + TestUtils.FILE_SET
                + "'\n"
                + "      buildResult 'SUCCESS'\n"
                + "        }\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      fileSet '"
                + TestUtils.FILE_SET
                + "'\n"
                + "        }\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      fileSet '"
                + TestUtils.FILE_SET
                + "'\n"
                + "      buildResult 'UNSTABLE'\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
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
        rule.assertLogContains("Setting build result to 'UNSTABLE'.", build);
    }

    @Test
    public void successIfNotFoundInFile() throws Exception {
        FreeStyleProject project = createProjectFromDsl("  steps {\n"
                + "    testWriteFileBuilder {\n"
                + "      file '"
                + TestUtils.FILE_SET
                + "'\n"
                + "      text 'foobaz'\n"
                + "    }\n"
                + "  }\n"
                + "  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      fileSet '"
                + TestUtils.FILE_SET
                + "'\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
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
        FreeStyleProject project = createProjectFromDsl("  steps {\n"
                + "    testWriteFileBuilder {\n"
                + "      file '"
                + TestUtils.FILE_SET
                + "'\n"
                + "      text 'foobaz'\n"
                + "    }\n"
                + "  }\n"
                + "  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      fileSet '"
                + TestUtils.FILE_SET
                + "'\n"
                + "      changeCondition 'MATCH_NOT_FOUND'\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
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
        FreeStyleProject project = createProjectFromDsl("  steps {\n"
                + "    testEchoBuilder {\n"
                + "      message '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "    }\n"
                + "  }\n"
                + "  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      buildResult 'SUCCESS'\n"
                + "      alsoCheckConsoleOutput true\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
        FreeStyleBuild build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
    }

    @Test
    public void failureIfFoundInConsole() throws Exception {
        FreeStyleProject project = createProjectFromDsl("  steps {\n"
                + "    testEchoBuilder {\n"
                + "      message '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "    }\n"
                + "  }\n"
                + "  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      alsoCheckConsoleOutput true\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
    }

    @Test
    public void unstableIfFoundInConsole() throws Exception {
        FreeStyleProject project = createProjectFromDsl("  steps {\n"
                + "    testEchoBuilder {\n"
                + "      message '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "    }\n"
                + "  }\n"
                + "  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      buildResult 'UNSTABLE'\n"
                + "      alsoCheckConsoleOutput true\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.UNSTABLE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'UNSTABLE'.", build);
    }

    @Test
    public void notBuiltIfFoundInConsole() throws Exception {
        FreeStyleProject project = createProjectFromDsl("  steps {\n"
                + "    testEchoBuilder {\n"
                + "      message '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "    }\n"
                + "  }\n"
                + "  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      buildResult 'NOT_BUILT'\n"
                + "      alsoCheckConsoleOutput true\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.NOT_BUILT, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'NOT_BUILT'.", build);
    }

    @Test
    public void abortedIfFoundInConsole() throws Exception {
        FreeStyleProject project = createProjectFromDsl("  steps {\n"
                + "    testEchoBuilder {\n"
                + "      message '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "    }\n"
                + "  }\n"
                + "  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      buildResult 'ABORTED'\n"
                + "      alsoCheckConsoleOutput true\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.ABORTED, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'ABORTED'.", build);
    }

    @Test
    public void successIfNotFoundInConsole() throws Exception {
        FreeStyleProject project = createProjectFromDsl("  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      alsoCheckConsoleOutput true\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
        FreeStyleBuild build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
    }

    @Test
    public void failureIfNotFoundInConsole() throws Exception {
        FreeStyleProject project = createProjectFromDsl("  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      alsoCheckConsoleOutput true\n"
                + "      changeCondition 'MATCH_NOT_FOUND'\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
    }

    @Test
    public void multipleTextFindersInConsole() throws Exception {
        FreeStyleProject project = createProjectFromDsl("  steps {\n"
                + "    testEchoBuilder {\n"
                + "      message '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "    }\n"
                + "  }\n"
                + "  publishers {\n"
                + "    findText {\n"
                + "      textFinders {\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      buildResult 'SUCCESS'\n"
                + "      alsoCheckConsoleOutput true\n"
                + "        }\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      alsoCheckConsoleOutput true\n"
                + "        }\n"
                + "        textFinder {\n"
                + "          regexp '"
                + TestUtils.UNIQUE_TEXT
                + "'\n"
                + "      buildResult 'UNSTABLE'\n"
                + "      alsoCheckConsoleOutput true\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n");
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
        rule.assertLogContains("Setting build result to 'UNSTABLE'.", build);
    }

    private FreeStyleProject createProjectFromDsl(String dsl) throws Exception {
        FreeStyleProject seed = rule.createFreeStyleProject();
        ExecuteDslScripts executeDslScripts = new ExecuteDslScripts();
        String generatedJobName = "test" + rule.jenkins.getItems().size();
        executeDslScripts.setScriptText("job('" + generatedJobName + "') {\n" + dsl + "}\n");
        executeDslScripts.setUseScriptText(true);
        seed.getBuildersList().add(executeDslScripts);
        rule.buildAndAssertSuccess(seed);
        return rule.jenkins.getItemByFullName(generatedJobName, FreeStyleProject.class);
    }
}
