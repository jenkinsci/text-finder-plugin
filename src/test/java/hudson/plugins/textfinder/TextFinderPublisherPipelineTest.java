package hudson.plugins.textfinder;

import hudson.Functions;
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
    private static final String fileSet = "out.txt";

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
    public void successIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {writeFile file: '"
                                + fileSet
                                + "', text: '"
                                + UNIQUE_TEXT
                                + "'}\n"
                                + "node {findText regexp: '"
                                + UNIQUE_TEXT
                                + "', fileSet: '"
                                + fileSet
                                + "', succeedIfFound: true}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains(
                "[Text Finder] Looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the files at "
                        + "'"
                        + fileSet
                        + "'",
                build);
        assertLogContainsMatch(new File(getWorkspace(build), fileSet), UNIQUE_TEXT, build, false);
        rule.assertBuildStatus(Result.SUCCESS, build);
    }

    @Test
    public void failureIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {writeFile file: '"
                                + fileSet
                                + "', text: '"
                                + UNIQUE_TEXT
                                + "'}\n"
                                + "node {findText regexp: '"
                                + UNIQUE_TEXT
                                + "', fileSet: '"
                                + fileSet
                                + "'}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains(
                "[Text Finder] Looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the files at "
                        + "'"
                        + fileSet
                        + "'",
                build);
        assertLogContainsMatch(new File(getWorkspace(build), fileSet), UNIQUE_TEXT, build, false);
        rule.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    public void unstableIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {writeFile file: '"
                                + fileSet
                                + "', text: '"
                                + UNIQUE_TEXT
                                + "'}\n"
                                + "node {findText regexp: '"
                                + UNIQUE_TEXT
                                + "', fileSet: '"
                                + fileSet
                                + "', unstableIfFound: true}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains(
                "[Text Finder] Looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the files at "
                        + "'"
                        + fileSet
                        + "'",
                build);
        assertLogContainsMatch(new File(getWorkspace(build), fileSet), UNIQUE_TEXT, build, false);
        rule.assertBuildStatus(Result.UNSTABLE, build);
    }

    @Test
    public void notBuiltIfFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {writeFile file: '"
                                + fileSet
                                + "', text: '"
                                + UNIQUE_TEXT
                                + "'}\n"
                                + "node {findText regexp: '"
                                + UNIQUE_TEXT
                                + "', fileSet: '"
                                + fileSet
                                + "', notBuiltIfFound: true}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains(
                "[Text Finder] Looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the files at "
                        + "'"
                        + fileSet
                        + "'",
                build);
        assertLogContainsMatch(new File(getWorkspace(build), fileSet), UNIQUE_TEXT, build, false);
        rule.assertBuildStatus(Result.NOT_BUILT, build);
    }

    @Test
    public void notFoundInFile() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {writeFile file: '"
                                + fileSet
                                + "', text: 'foobaz'}\n"
                                + "node {findText regexp: '"
                                + UNIQUE_TEXT
                                + "', fileSet: '"
                                + fileSet
                                + "'}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains(
                "[Text Finder] Looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the files at "
                        + "'"
                        + fileSet
                        + "'",
                build);
        rule.assertBuildStatus(Result.SUCCESS, build);
    }

    @Test
    public void successIfFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {isUnix() ? sh('"
                                + ECHO_UNIQUE_TEXT
                                + "') : bat(\"prompt \\$G\\r\\n"
                                + ECHO_UNIQUE_TEXT
                                + "\")}\n"
                                + "node {findText regexp: '"
                                + UNIQUE_TEXT
                                + "', succeedIfFound: true, alsoCheckConsoleOutput: true}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the console output",
                build);
        assertLogContainsMatch(build.getLogFile(), ECHO_UNIQUE_TEXT, build, true);
        rule.assertBuildStatus(Result.SUCCESS, build);
    }

    @Test
    public void failureIfFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {isUnix() ? sh('"
                                + ECHO_UNIQUE_TEXT
                                + "') : bat(\"prompt \\$G\\r\\n"
                                + ECHO_UNIQUE_TEXT
                                + "\")}\n"
                                + "node {findText regexp: '"
                                + UNIQUE_TEXT
                                + "', alsoCheckConsoleOutput: true}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the console output",
                build);
        assertLogContainsMatch(build.getLogFile(), ECHO_UNIQUE_TEXT, build, true);
        rule.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    public void unstableIfFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {isUnix() ? sh('"
                                + ECHO_UNIQUE_TEXT
                                + "') : bat(\"prompt \\$G\\r\\n"
                                + ECHO_UNIQUE_TEXT
                                + "\")}\n"
                                + "node {findText regexp: '"
                                + UNIQUE_TEXT
                                + "', unstableIfFound: true, alsoCheckConsoleOutput: true}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the console output",
                build);
        assertLogContainsMatch(build.getLogFile(), ECHO_UNIQUE_TEXT, build, true);
        rule.assertBuildStatus(Result.UNSTABLE, build);
    }

    @Test
    public void notBuiltIfFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {isUnix() ? sh('"
                                + ECHO_UNIQUE_TEXT
                                + "') : bat(\"prompt \\$G\\r\\n"
                                + ECHO_UNIQUE_TEXT
                                + "\")}\n"
                                + "node {findText regexp: '"
                                + UNIQUE_TEXT
                                + "', notBuiltIfFound: true, alsoCheckConsoleOutput: true}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the console output",
                build);
        assertLogContainsMatch(build.getLogFile(), ECHO_UNIQUE_TEXT, build, true);
        rule.assertBuildStatus(Result.NOT_BUILT, build);
    }

    @Test
    public void notFoundInConsole() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {findText regexp: '"
                                + UNIQUE_TEXT
                                + "', alsoCheckConsoleOutput: true}\n"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("[Text Finder] Scanning console output...", build);
        rule.assertLogContains(
                "[Text Finder] Finished looking for pattern '"
                        + UNIQUE_TEXT
                        + "' in the console output",
                build);
        rule.assertBuildStatus(Result.SUCCESS, build);
    }

    @Test
    public void lastFinderWins() throws Exception {
        WorkflowJob project = rule.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(
                new CpsFlowDefinition(
                        "node {isUnix() ? sh('echo foobar') : bat(\"prompt \\$G\\r\\necho foobar\")}\n"
                                + "node {"
                                + "findText regexp: 'foobar', alsoCheckConsoleOutput: true\n"
                                + "findText regexp: 'foobar', unstableIfFound: true, alsoCheckConsoleOutput: true\n"
                                + "findText regexp: 'foobar', notBuiltIfFound: true, alsoCheckConsoleOutput: true\n"
                                + "}"));
        WorkflowRun build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("Checking console output", build);
        assertLogContainsMatch(build.getLogFile(), ECHO_UNIQUE_TEXT, build, true);
        rule.assertBuildStatus(Result.NOT_BUILT, build);
    }
}
