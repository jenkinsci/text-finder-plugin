package hudson.plugins.textfinder;

import hudson.model.Result;
import hudson.slaves.DumbSlave;
import java.io.File;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class TextFinderPublisherAgentTest {

    @Test
    void failureIfFoundInFileOnAgent(JenkinsRule rule) throws Exception {
        DumbSlave agent = rule.createOnlineSlave();
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node('"
                        + agent.getNodeName()
                        + "') {\n"
                        + "  writeFile file: '"
                        + TestUtils.FILE_SET
                        + "', text: 'foobar'\n"
                        + "  findText(textFinders: [textFinder(regexp: 'foobar', fileSet: '"
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
    void failureIfFoundInConsoleOnAgent(JenkinsRule rule) throws Exception {
        DumbSlave agent = rule.createOnlineSlave();
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(
                "node('"
                        + agent.getNodeName()
                        + "') {\n"
                        + "  testEcho '"
                        + TestUtils.UNIQUE_TEXT
                        + "'\n"
                        + "  findText(textFinders: [textFinder(regexp: 'foobar',"
                        + " alsoCheckConsoleOutput: true)])\n"
                        + "}\n",
                true));
        WorkflowRun build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains("Setting build result to 'FAILURE'.", build);
    }
}
