package hudson.plugins.textfinder;

import hudson.Functions;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.BatchFile;
import hudson.tasks.CommandInterpreter;
import hudson.tasks.Shell;
import java.io.File;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class TextFinderPublisherFreestyleTest {

    private static final String UNIQUE_TEXT = "foobar";
    private static final String ECHO_UNIQUE_TEXT = "echo " + UNIQUE_TEXT;

    @Rule public JenkinsRule rule = new JenkinsRule();

    private void assertLogContainsMatch(
            File file, String text, FreeStyleBuild build, boolean isShell) throws IOException {
        String prompt;
        if (isShell) {
            prompt = Functions.isWindows() ? "> " : "+ ";
        } else {
            prompt = "";
        }
        rule.assertLogContains(
                String.format(
                        "%s:%s%s%s", file, System.getProperty("line.separator"), prompt, text),
                build);
    }

    @Test
    public void successIfFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject("freestyle");
        CommandInterpreter command =
                Functions.isWindows()
                        ? new BatchFile("prompt $G\\r\\n" + ECHO_UNIQUE_TEXT)
                        : new Shell(ECHO_UNIQUE_TEXT);
        project.getBuildersList().add(command);
        TextFinderPublisher textFinder = new TextFinderPublisher(UNIQUE_TEXT);
        textFinder.setSucceedIfFound(true);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("Checking console output", build);
        assertLogContainsMatch(build.getLogFile(), ECHO_UNIQUE_TEXT, build, true);
        rule.assertBuildStatus(Result.SUCCESS, build);
    }

    @Test
    public void failureIfFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject("freestyle");
        CommandInterpreter command =
                Functions.isWindows()
                        ? new BatchFile("prompt $G\\r\\n" + ECHO_UNIQUE_TEXT)
                        : new Shell(ECHO_UNIQUE_TEXT);
        project.getBuildersList().add(command);
        TextFinderPublisher textFinder = new TextFinderPublisher(UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("Checking console output", build);
        assertLogContainsMatch(build.getLogFile(), ECHO_UNIQUE_TEXT, build, true);
        rule.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    public void unstableIfFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject("freestyle");
        CommandInterpreter command =
                Functions.isWindows()
                        ? new BatchFile("prompt $G\\r\\n" + ECHO_UNIQUE_TEXT)
                        : new Shell(ECHO_UNIQUE_TEXT);
        project.getBuildersList().add(command);
        TextFinderPublisher textFinder = new TextFinderPublisher(UNIQUE_TEXT);
        textFinder.setUnstableIfFound(true);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("Checking console output", build);
        assertLogContainsMatch(build.getLogFile(), ECHO_UNIQUE_TEXT, build, true);
        rule.assertBuildStatus(Result.UNSTABLE, build);
    }

    @Test
    public void notFoundInConsole() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject("freestyle");
        TextFinderPublisher textFinder = new TextFinderPublisher(UNIQUE_TEXT);
        textFinder.setAlsoCheckConsoleOutput(true);
        project.getPublishersList().add(textFinder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        rule.waitForCompletion(build);
        rule.assertLogContains("Checking console output", build);
        rule.assertBuildStatus(Result.SUCCESS, build);
    }
}
