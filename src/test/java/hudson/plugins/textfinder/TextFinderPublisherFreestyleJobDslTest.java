package hudson.plugins.textfinder;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

import javaposse.jobdsl.plugin.ExecuteDslScripts;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;

public class TextFinderPublisherFreestyleJobDslTest {

    @Rule public JenkinsRule rule = new JenkinsRule();

    @Test
    public void successIfFoundInFile() throws Exception {
        FreeStyleProject project =
                createProjectFromDsl(
                        "  steps {\n"
                                + "    testWriteFileBuilder {"
                                + "      file '"
                                + TestUtils.FILE_SET
                                + "'\n"
                                + "      text '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "    }\n"
                                + "  }\n"
                                + "  publishers {\n"
                                + "    findText {"
                                + "      regexp '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "      fileSet '"
                                + TestUtils.FILE_SET
                                + "'\n"
                                + "      succeedIfFound true\n"
                                + "    }\n"
                                + "  }\n");
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
        FreeStyleProject project =
                createProjectFromDsl(
                        "  steps {\n"
                                + "    testWriteFileBuilder {"
                                + "      file '"
                                + TestUtils.FILE_SET
                                + "'\n"
                                + "      text '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "    }\n"
                                + "  }\n"
                                + "  publishers {\n"
                                + "    findText {"
                                + "      regexp '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "      fileSet '"
                                + TestUtils.FILE_SET
                                + "'\n"
                                + "    }\n"
                                + "  }\n");
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
        FreeStyleProject project =
                createProjectFromDsl(
                        "  steps {\n"
                                + "    testWriteFileBuilder {"
                                + "      file '"
                                + TestUtils.FILE_SET
                                + "'\n"
                                + "      text '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "    }\n"
                                + "  }\n"
                                + "  publishers {\n"
                                + "    findText {"
                                + "      regexp '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "      fileSet '"
                                + TestUtils.FILE_SET
                                + "'\n"
                                + "      unstableIfFound true\n"
                                + "    }\n"
                                + "  }\n");
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
        FreeStyleProject project =
                createProjectFromDsl(
                        "  steps {\n"
                                + "    testWriteFileBuilder {"
                                + "      file '"
                                + TestUtils.FILE_SET
                                + "'\n"
                                + "      text '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "    }\n"
                                + "  }\n"
                                + "  publishers {\n"
                                + "    findText {"
                                + "      regexp '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "      fileSet '"
                                + TestUtils.FILE_SET
                                + "'\n"
                                + "      notBuiltIfFound true\n"
                                + "    }\n"
                                + "  }\n");
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
        FreeStyleProject project =
                createProjectFromDsl(
                        "  steps {\n"
                                + "    testWriteFileBuilder {"
                                + "      file '"
                                + TestUtils.FILE_SET
                                + "'\n"
                                + "      text 'foobaz'\n"
                                + "    }\n"
                                + "  }\n"
                                + "  publishers {\n"
                                + "    findText {"
                                + "      regexp '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "      fileSet '"
                                + TestUtils.FILE_SET
                                + "'\n"
                                + "    }\n"
                                + "  }\n");
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
        FreeStyleProject project =
                createProjectFromDsl(
                        "  steps {\n"
                                + "    testEchoBuilder {"
                                + "      message '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "    }\n"
                                + "  }\n"
                                + "  publishers {\n"
                                + "    findText {"
                                + "      regexp '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "      succeedIfFound true\n"
                                + "      alsoCheckConsoleOutput true\n"
                                + "    }\n"
                                + "  }\n");
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
        FreeStyleProject project =
                createProjectFromDsl(
                        "  steps {\n"
                                + "    testEchoBuilder {"
                                + "      message '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "    }\n"
                                + "  }\n"
                                + "  publishers {\n"
                                + "    findText {"
                                + "      regexp '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "      alsoCheckConsoleOutput true\n"
                                + "    }\n"
                                + "  }\n");
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
        FreeStyleProject project =
                createProjectFromDsl(
                        "  steps {\n"
                                + "    testEchoBuilder {"
                                + "      message '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "    }\n"
                                + "  }\n"
                                + "  publishers {\n"
                                + "    findText {"
                                + "      regexp '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "      unstableIfFound true\n"
                                + "      alsoCheckConsoleOutput true\n"
                                + "    }\n"
                                + "  }\n");
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
    public void notBuiltIfFoundInConsole() throws Exception {
        FreeStyleProject project =
                createProjectFromDsl(
                        "  steps {\n"
                                + "    testEchoBuilder {"
                                + "      message '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "    }\n"
                                + "  }\n"
                                + "  publishers {\n"
                                + "    findText {"
                                + "      regexp '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "      notBuiltIfFound true\n"
                                + "      alsoCheckConsoleOutput true\n"
                                + "    }\n"
                                + "  }\n");
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.NOT_BUILT, project);
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
        FreeStyleProject project =
                createProjectFromDsl(
                        "  publishers {\n"
                                + "    findText {"
                                + "      regexp '"
                                + TestUtils.UNIQUE_TEXT
                                + "'\n"
                                + "      alsoCheckConsoleOutput true\n"
                                + "    }\n"
                                + "  }\n");
        FreeStyleBuild build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in the console output",
                build);
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
