package hudson.plugins.textfinder;

import hudson.Functions;
import hudson.model.Result;
import hudson.slaves.DumbSlave;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jenkinsci.plugins.workflow.actions.WorkspaceAction;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.graph.FlowGraphWalker;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class TextFinderPublisherAgentTest {

    private static final String UNIQUE_TEXT = "foobar";
    private static final String ECHO_UNIQUE_TEXT = "echo " + UNIQUE_TEXT;

    @Rule public JenkinsRule rule = new JenkinsRule();

    private void assertLogContainsMatch(File file, String text, WorkflowRun build, boolean isShell)
            throws IOException {
        String prompt;
        if (isShell) {
            prompt = Functions.isWindows() ? ">" : "+ ";
        } else {
            prompt = "";
        }
        rule.assertLogContains(
                String.format(
                        "%s:%s%s%s", file, System.getProperty("line.separator"), prompt, text),
                build);
    }

    private File getWorkspace(WorkflowRun build) {
        FlowGraphWalker walker = new FlowGraphWalker(build.getExecution());
        List<WorkspaceAction> actions = new ArrayList<>();
        for (FlowNode node : walker) {
            WorkspaceAction action = node.getAction(WorkspaceAction.class);
            if (action != null) {
                return new File(action.getWorkspace().getRemote());
            }
        }
        throw new IllegalStateException("Failed to find workspace");
    }

    @Test
    public void failureIfFoundInFileOnAgent() throws Exception {
        DumbSlave agent = rule.createOnlineSlave();
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        String.format(
                                "node('%s') {writeFile file: 'out.txt', text: 'foobar'}\n"
                                        + "node('%s') {findText regexp: 'foobar', fileSet: 'out.txt'}\n",
                                agent.getNodeName(), agent.getNodeName())));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("Looking for pattern " + "'" + UNIQUE_TEXT + "'" + " in the file " + "'" + build.getLogFile() + "'", build);
        assertLogContainsMatch(new File(getWorkspace(build), "out.txt"), UNIQUE_TEXT, build, false);
        rule.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    public void failureIfFoundInConsoleOnAgent() throws Exception {
        DumbSlave agent = rule.createOnlineSlave();
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        String.format(
                                "node('%s') {isUnix() ? sh('echo foobar') : bat(\"prompt \\$G\\r\\necho foobar\")}\n"
                                        + "node('%s') {findText regexp: 'foobar', alsoCheckConsoleOutput: true}\n",
                                agent.getNodeName(), agent.getNodeName())));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("Looking for pattern " + "'" + ECHO_UNIQUE_TEXT + "'" + " in the console output", build);
        assertLogContainsMatch(build.getLogFile(), ECHO_UNIQUE_TEXT, build, true);
        rule.assertBuildStatus(Result.FAILURE, build);
    }
}
