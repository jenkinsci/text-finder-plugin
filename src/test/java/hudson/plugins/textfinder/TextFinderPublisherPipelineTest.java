package hudson.plugins.textfinder;

import hudson.model.Result;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;

public class TextFinderPublisherPipelineTest {

    @Rule public JenkinsRule rule = new JenkinsRule();

    @Test
    public void successIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(
                new CpsFlowDefinition(
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
                "[Text Finder] Looking for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in the files at "
                        + "'"
                        + TestUtils.FILE_SET
                        + "'",
                build);
        TestUtils.assertFileContainsMatch(
                new File(TestUtils.getWorkspace(build), TestUtils.FILE_SET),
                TestUtils.UNIQUE_TEXT,
                rule,
                build);
    }

    @Test
    public void failureIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(
                new CpsFlowDefinition(
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
                "[Text Finder] Looking for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in the files at "
                        + "'"
                        + TestUtils.FILE_SET
                        + "'",
                build);
        TestUtils.assertFileContainsMatch(
                new File(TestUtils.getWorkspace(build), TestUtils.FILE_SET),
                TestUtils.UNIQUE_TEXT,
                rule,
                build);
    }

    @Test
    public void unstableIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(
                new CpsFlowDefinition(
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
                "[Text Finder] Looking for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in the files at "
                        + "'"
                        + TestUtils.FILE_SET
                        + "'",
                build);
        TestUtils.assertFileContainsMatch(
                new File(TestUtils.getWorkspace(build), TestUtils.FILE_SET),
                TestUtils.UNIQUE_TEXT,
                rule,
                build);
    }

    @Test
    public void notBuiltIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(
                new CpsFlowDefinition(
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
                "[Text Finder] Looking for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in the files at "
                        + "'"
                        + TestUtils.FILE_SET
                        + "'",
                build);
        TestUtils.assertFileContainsMatch(
                new File(TestUtils.getWorkspace(build), TestUtils.FILE_SET),
                TestUtils.UNIQUE_TEXT,
                rule,
                build);
    }

    @Test
    public void notFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(
                new CpsFlowDefinition(
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
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(
                new CpsFlowDefinition(
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
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(
                new CpsFlowDefinition(
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
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(
                new CpsFlowDefinition(
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
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(
                new CpsFlowDefinition(
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
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {\n"
                                + "  findText regexp: '"
                                + TestUtils.UNIQUE_TEXT
                                + "', alsoCheckConsoleOutput: true\n"
                                + "}\n",
                        true));
        WorkflowRun build = rule.buildAndAssertSuccess(project);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + TestUtils.UNIQUE_TEXT
                        + "' in the console output",
                build);
    }
}
