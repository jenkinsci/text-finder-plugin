package hudson.plugins.textfinder;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import java.io.File;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class TextFinderPublisherFreestyleJobDslTest {

    @Test
    void successIfFoundInFile(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  steps {\n"
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
    void failureIfFoundInFile(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  steps {\n"
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
    void unstableIfFoundInFile(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  steps {\n"
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
    void notBuiltIfFoundInFile(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  steps {\n"
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
    void abortedIfFoundInFile(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  steps {\n"
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
    void multipleTextFindersInFile(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  steps {\n"
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
    void successIfNotFoundInFile(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  steps {\n"
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
    void failureIfNotFoundInFile(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  steps {\n"
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
    void successIfFoundInConsole(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  steps {\n"
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
    void failureIfFoundInConsole(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  steps {\n"
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
    void unstableIfFoundInConsole(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  steps {\n"
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
    void notBuiltIfFoundInConsole(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  steps {\n"
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
    void abortedIfFoundInConsole(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  steps {\n"
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
    void successIfNotFoundInConsole(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  publishers {\n"
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
    void failureIfNotFoundInConsole(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  publishers {\n"
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
    void multipleTextFindersInConsole(JenkinsRule rule) throws Exception {
        FreeStyleProject project = createProjectFromDsl(
                rule,
                "  steps {\n"
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

    private static FreeStyleProject createProjectFromDsl(JenkinsRule rule, String dsl) throws Exception {
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
