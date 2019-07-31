package hudson.plugins.textfinder;

import static org.junit.Assert.assertNotNull;

import hudson.Functions;
import hudson.model.Run;
import java.io.File;
import java.io.IOException;
import org.jenkinsci.plugins.workflow.actions.WorkspaceAction;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowGraphWalker;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jvnet.hudson.test.JenkinsRule;

/** Utilities for testing Text Finder */
public class TestUtils {

    private static void assertContainsMatch(
            String header, String text, JenkinsRule rule, Run<?, ?> build, boolean isShell)
            throws IOException {
        String prompt;
        if (isShell) {
            prompt = Functions.isWindows() ? ">" : "+ ";
        } else {
            prompt = "";
        }
        rule.assertLogContains(String.format("%s%s%s", header, prompt, text), build);
    }

    public static void assertConsoleContainsMatch(
            String text, JenkinsRule rule, Run<?, ?> build, boolean isShell) throws IOException {
        assertContainsMatch("", text, rule, build, isShell);
    }

    public static void assertFileContainsMatch(
            File file, String text, JenkinsRule rule, Run<?, ?> build, boolean isShell)
            throws IOException {
        assertContainsMatch(
                String.format("%s:%s", file, System.getProperty("line.separator")),
                text,
                rule,
                build,
                isShell);
    }

    public static File getWorkspace(WorkflowRun build) {
        FlowExecution execution = build.getExecution();
        assertNotNull(execution);
        FlowGraphWalker walker = new FlowGraphWalker(execution);
        for (FlowNode node : walker) {
            WorkspaceAction action = node.getAction(WorkspaceAction.class);
            if (action != null) {
                return new File(action.getWorkspace().getRemote());
            }
        }
        throw new IllegalStateException("Failed to find workspace");
    }
}
