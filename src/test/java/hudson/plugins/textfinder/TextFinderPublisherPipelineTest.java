package hudson.plugins.textfinder;

import hudson.model.Result;
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

public class TextFinderPublisherPipelineTest {

    private static final String UNIQUE_TEXT = "foobar";
    private static final String ECHO_UNIQUE_TEXT = "echo " + UNIQUE_TEXT;
    private static final String LOG_UNIQUE_TEXT = "+ " + ECHO_UNIQUE_TEXT;

    @Rule public JenkinsRule rule = new JenkinsRule();

    private void assertLogContainsMatch(File file, String text, WorkflowRun build)
            throws IOException {
        rule.assertLogContains(
                String.format("%s:%s%s", file, System.getProperty("line.separator"), text), build);
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
    public void successIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {writeFile file: 'out.txt', text: 'foobar'}\n"
                                + "node {findText regexp: 'foobar', fileSet: 'out.txt', succeedIfFound: true}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("Checking foobar", build);
        assertLogContainsMatch(new File(getWorkspace(build), "out.txt"), UNIQUE_TEXT, build);
        rule.assertBuildStatus(Result.SUCCESS, build);
    }

    @Test
    public void failureIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {writeFile file: 'out.txt', text: 'foobar'}\n"
                                + "node {findText regexp: 'foobar', fileSet: 'out.txt'}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("Checking foobar", build);
        assertLogContainsMatch(new File(getWorkspace(build), "out.txt"), UNIQUE_TEXT, build);
        rule.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    public void unstableIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {writeFile file: 'out.txt', text: 'foobar'}\n"
                                + "node {findText regexp: 'foobar', fileSet: 'out.txt', unstableIfFound: true}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("Checking foobar", build);
        assertLogContainsMatch(new File(getWorkspace(build), "out.txt"), UNIQUE_TEXT, build);
        rule.assertBuildStatus(Result.UNSTABLE, build);
    }

    @Test
    public void notFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {writeFile file: 'out.txt', text: 'foobaz'}\n"
                                + "node {findText regexp: 'foobar', fileSet: 'out.txt'}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("Checking foobar", build);
        rule.assertBuildStatus(Result.SUCCESS, build);
    }

    @Test
    public void successIfFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {isUnix() ? sh('echo foobar') : bat('echo foobar')}\n"
                                + "node {findText regexp: 'foobar', succeedIfFound: true, alsoCheckConsoleOutput: true}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("Checking console output", build);
        assertLogContainsMatch(build.getLogFile(), LOG_UNIQUE_TEXT, build);
        rule.assertBuildStatus(Result.SUCCESS, build);
    }

    @Test
    public void failureIfFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {isUnix() ? sh('echo foobar') : bat('echo foobar')}\n"
                                + "node {findText regexp: 'foobar', alsoCheckConsoleOutput: true}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("Checking console output", build);
        assertLogContainsMatch(build.getLogFile(), LOG_UNIQUE_TEXT, build);
        rule.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    public void unstableIfFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {isUnix() ? sh('echo foobar') : bat('echo foobar')}\n"
                                + "node {findText regexp: 'foobar', unstableIfFound: true, alsoCheckConsoleOutput: true}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("Checking console output", build);
        assertLogContainsMatch(build.getLogFile(), LOG_UNIQUE_TEXT, build);
        rule.assertBuildStatus(Result.UNSTABLE, build);
    }

    @Test
    public void notFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {findText regexp: 'foobar', alsoCheckConsoleOutput: true}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("Checking console output", build);
        rule.assertBuildStatus(Result.SUCCESS, build);
    }
}
