package hudson.plugins.textfinder;

import hudson.model.Result;
import java.io.File;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class TextFinderPublisherPipelineTest {

    @Rule
    public JenkinsRule rule = new JenkinsRule();

    @Test
    public void successIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  writeFile file: '"
                        + TestUtils.FILE_SET
                        + "', text: '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "', buildResult: 'SUCCESS')])\n"
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
    public void failureIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  writeFile file: '"
                        + TestUtils.FILE_SET
                        + "', text: '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "')])\n"
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
    public void unstableIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  writeFile file: '"
                        + TestUtils.FILE_SET
                        + "', text: '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "', buildResult: 'UNSTABLE')])\n"
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
    public void notBuiltIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  writeFile file: '"
                        + TestUtils.FILE_SET
                        + "', text: '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "', buildResult: 'NOT_BUILT')])\n"
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
    public void abortedIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  writeFile file: '"
                        + TestUtils.FILE_SET
                        + "', text: '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "', buildResult: 'ABORTED')])\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertStatus(Result.ABORTED, project);
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
        rule.assertLogContains("Setting build result to 'ABORTED'.", build);
    }

    @Test
    public void successIfNotFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  writeFile file: '"
                        + TestUtils.FILE_SET
                        + "', text: 'foobaz'\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "')])\n"
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
    public void failureIfNotFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  writeFile file: '"
                        + TestUtils.FILE_SET
                        + "', text: 'foobaz'\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "', changeCondition: 'MATCH_NOT_FOUND')])\n"
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
    public void multipleTextFindersInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  writeFile file: '"
                        + TestUtils.FILE_SET
                        + "', text: '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "', buildResult: 'SUCCESS'),\n"
                        + "      textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "'),\n"
                        + "      textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', fileSet: '"
                        + TestUtils.FILE_SET
                        + "', buildResult: 'UNSTABLE')])\n"
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
        rule.assertLogContains("Setting build result to 'UNSTABLE'.", build);
    }

    @Test
    public void successIfFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "  testEcho '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "node {\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', buildResult: 'SUCCESS', alsoCheckConsoleOutput: true)])\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
    }

    @Test
    public void failureIfFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "  testEcho '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "node {\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', alsoCheckConsoleOutput: true)])\n"
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
    public void unstableIfFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "  testEcho '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "node {\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', buildResult: 'UNSTABLE', alsoCheckConsoleOutput: true)])\n"
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
    public void notBuiltIfFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "  testEcho '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "node {\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', buildResult: 'NOT_BUILT', alsoCheckConsoleOutput: true)])\n"
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
    public void abortedBuiltIfFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "  testEcho '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "node {\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', buildResult: 'ABORTED', alsoCheckConsoleOutput: true)])\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertStatus(Result.ABORTED, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'ABORTED'.", build);
    }

    @Test
    public void successIfNotFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', alsoCheckConsoleOutput: true)])\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
    }

    @Test
    public void failureIfNotFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', alsoCheckConsoleOutput: true, changeCondition:"
                        + " 'MATCH_NOT_FOUND')])\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
    }

    @Test
    public void multipleTextFindersInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "  testEcho '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "node {\n"
                        + "  findText(textFinders: [textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', buildResult: 'SUCCESS', alsoCheckConsoleOutput: true),\n"
                        + "      textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', alsoCheckConsoleOutput: true),\n"
                        + "      textFinder(regexp: '"
                        + TestUtils.UNIQUE_TEXT
                        + "', buildResult: 'UNSTABLE', alsoCheckConsoleOutput: true)])\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
        rule.assertLogContains("Setting build result to 'UNSTABLE'.", build);
    }
}
