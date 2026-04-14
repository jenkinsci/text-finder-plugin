package hudson.plugins.textfinder;

import hudson.model.Result;
import java.io.File;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class TextFinderPublisherPipelineCompatibilityTest {

    @Test
    void successIfFoundInFile(JenkinsRule rule) throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  writeFile file: '"
                        + TestUtils.FILE_SET
                        + "', text: '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "  findText regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "', succeedIfFound: true\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains(
                "[Text Finder] Searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        TestUtils.assertFileContainsMatch(
                new File(TestUtils.getWorkspace(build), TestUtils.FILE_SET), TestUtils.UNIQUE_TEXT, rule, build);
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
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  writeFile file: '"
                        + TestUtils.FILE_SET
                        + "', text: '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "  findText regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "'\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains(
                "[Text Finder] Searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        TestUtils.assertFileContainsMatch(
                new File(TestUtils.getWorkspace(build), TestUtils.FILE_SET), TestUtils.UNIQUE_TEXT, rule, build);
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
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  writeFile file: '"
                        + TestUtils.FILE_SET
                        + "', text: '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "  findText regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "', unstableIfFound: true\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertStatus(Result.UNSTABLE, project);
        rule.assertLogContains(
                "[Text Finder] Searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        TestUtils.assertFileContainsMatch(
                new File(TestUtils.getWorkspace(build), TestUtils.FILE_SET), TestUtils.UNIQUE_TEXT, rule, build);
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
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  writeFile file: '"
                        + TestUtils.FILE_SET
                        + "', text: '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "  findText regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "', notBuiltIfFound: true\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertStatus(Result.NOT_BUILT, project);
        rule.assertLogContains(
                "[Text Finder] Searching for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in file set '"
                        + TestUtils.FILE_SET
                        + "'.",
                build);
        TestUtils.assertFileContainsMatch(
                new File(TestUtils.getWorkspace(build), TestUtils.FILE_SET), TestUtils.UNIQUE_TEXT, rule, build);
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
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  writeFile file: '"
                        + TestUtils.FILE_SET
                        + "', text: 'foobaz'\n"
                        + "  findText regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "'\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertSuccess(project);
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
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  writeFile file: '"
                        + TestUtils.FILE_SET
                        + "', text: 'foobaz'\n"
                        + "  findText regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "', succeedIfFound: true\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertStatus(Result.FAILURE, project);
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
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "  testEcho '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "node {\n"
                        + "  findText regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', succeedIfFound: true, alsoCheckConsoleOutput: true\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
    }

    @Test
    void failureIfFoundInConsole(JenkinsRule rule) throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "  testEcho '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "node {\n"
                        + "  findText regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', alsoCheckConsoleOutput: true\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
    }

    @Test
    void unstableIfFoundInConsole(JenkinsRule rule) throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "  testEcho '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "node {\n"
                        + "  findText regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', unstableIfFound: true, alsoCheckConsoleOutput: true\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertStatus(Result.UNSTABLE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'UNSTABLE'.", build);
    }

    @Test
    void notBuiltIfFoundInConsole(JenkinsRule rule) throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "  testEcho '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "node {\n"
                        + "  findText regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', notBuiltIfFound: true, alsoCheckConsoleOutput: true\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertStatus(Result.NOT_BUILT, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'NOT_BUILT'.", build);
    }

    @Test
    void successIfNotFoundInConsole(JenkinsRule rule) throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  findText regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', alsoCheckConsoleOutput: true\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
    }

    @Test
    void failureIfNotFoundInConsole(JenkinsRule rule) throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  findText regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', alsoCheckConsoleOutput: true, succeedIfFound: true\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
    }
}
