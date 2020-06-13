package hudson.plugins.textfinder;

import hudson.model.Result;
import hudson.slaves.DumbSlave;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;

public class TextFinderPublisherAgentTest {

    private static final String UNIQUE_TEXT = "foobar";
    private static final String ECHO_UNIQUE_TEXT = "echo " + UNIQUE_TEXT;

    @Rule public JenkinsRule rule = new JenkinsRule();

    @Test
    public void failureIfFoundInFileOnAgent() throws Exception {
        DumbSlave agent = rule.createOnlineSlave();
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(
                new CpsFlowDefinition(
                        String.format(
                                "node('%s') {\n"
                                        + "  writeFile file: 'out.txt', text: 'foobar'\n"
                                        + "  findText regexp: 'foobar', fileSet: 'out.txt'\n"
                                        + "}\n",
                                agent.getNodeName()),
                        true));
        WorkflowRun build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains(
                "[Text Finder] Looking for pattern " + "'" + UNIQUE_TEXT + "'" + " in the files at",
                build);
        TestUtils.assertFileContainsMatch(
                new File(TestUtils.getWorkspace(build), "out.txt"),
                UNIQUE_TEXT,
                rule,
                build,
                false);
    }

    @Test
    public void failureIfFoundInConsoleOnAgent() throws Exception {
        DumbSlave agent = rule.createOnlineSlave();
        WorkflowJob project = rule.createProject(WorkflowJob.class);
        project.setDefinition(
                new CpsFlowDefinition(
                        String.format(
                                "node('%s') {\n"
                                    + "  isUnix() ? sh('echo foobar') : bat(\"prompt \\$G\\r"
                                    + "\\n"
                                    + "echo foobar\")\n"
                                    + "  findText regexp: 'foobar', alsoCheckConsoleOutput: true\n"
                                    + "}\n",
                                agent.getNodeName()),
                        true));
        WorkflowRun build = rule.buildAndAssertStatus(Result.FAILURE, project);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the console output",
                build);
        TestUtils.assertConsoleContainsMatch(ECHO_UNIQUE_TEXT, rule, build, true);
    }
}
