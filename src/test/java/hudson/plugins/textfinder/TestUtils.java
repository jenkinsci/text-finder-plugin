package hudson.plugins.textfinder;

import static org.junit.Assert.assertNotNull;

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

    public static final String FILE_SET = "out.txt";
    public static final String PREFIX = ">>> ";
    public static final String UNIQUE_TEXT = "foobar";

    public static final String SUPER_ID = "superId";
    public static final String SUPER_ID_KEY = "future name: ";
    public static final String SUPER_ID_LINE = SUPER_ID_KEY + SUPER_ID;
    public static final String ECHO_ID = "echo " + SUPER_ID_LINE;
    public static final String ECHO_UNIQUE_TEXT = "echo " + UNIQUE_TEXT;

    public static void assertFileContainsMatch(File file, String text, JenkinsRule rule, Run<?, ?> build)
            throws IOException {
        rule.assertLogContains(String.format("%s:%s%s", file, System.getProperty("line.separator"), text), build);
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
