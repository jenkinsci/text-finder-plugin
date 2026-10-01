package hudson.plugins.textfinder;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.textfinder.test.TestEchoBuilder;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

@SuppressWarnings("deprecation")
public class TextFinderPublisherFreestyleForceImprovementTest {

    @Rule
    public JenkinsRule rule = new JenkinsRule();

    @Test
    public void twoInRowCanGetWorseNonIsForcing() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.UNIQUE_TEXT));
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.ANOTHER_UNIQUE_TEXT));
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        List<TextFinder> textfinders = new ArrayList<>();
        TextFinder textFinder1 = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder1.setBuildResult(Result.UNSTABLE.toString());
        textFinder1.setAlsoCheckConsoleOutput(true);
        textfinders.add(textFinder1);
        TextFinder textFinder2 = new TextFinder(TestUtils.ANOTHER_UNIQUE_TEXT);
        textFinder2.setBuildResult(Result.NOT_BUILT.toString());
        textFinder2.setAlsoCheckConsoleOutput(true);
        textfinders.add(textFinder2);
        textFinderPublisher.setTextFinders(textfinders);
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.NOT_BUILT, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.ANOTHER_UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.ANOTHER_UNIQUE_TEXT + "' in console output.", build);
    }

    @Test
    public void twoInRowCanNotGetBetterNonIsForcing() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.UNIQUE_TEXT));
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.ANOTHER_UNIQUE_TEXT));
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        List<TextFinder> textfinders = new ArrayList<>();
        TextFinder textFinder1 = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder1.setBuildResult(Result.UNSTABLE.toString());
        textFinder1.setAlsoCheckConsoleOutput(true);
        textfinders.add(textFinder1);
        TextFinder textFinder2 = new TextFinder(TestUtils.ANOTHER_UNIQUE_TEXT);
        textFinder2.setBuildResult(Result.SUCCESS.toString());
        textFinder2.setAlsoCheckConsoleOutput(true);
        textfinders.add(textFinder2);
        textFinderPublisher.setTextFinders(textfinders);
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.UNSTABLE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.ANOTHER_UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.ANOTHER_UNIQUE_TEXT + "' in console output.", build);
    }

    @Test
    public void twoInRowCanGetBetterSecondIsForcing() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.UNIQUE_TEXT));
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.ANOTHER_UNIQUE_TEXT));
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        List<TextFinder> textfinders = new ArrayList<>();
        TextFinder textFinder1 = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder1.setBuildResult(Result.UNSTABLE.toString());
        textFinder1.setAlsoCheckConsoleOutput(true);
        textfinders.add(textFinder1);
        TextFinder textFinder2 = new TextFinder(TestUtils.ANOTHER_UNIQUE_TEXT);
        textFinder2.setBuildResult(Result.SUCCESS.toString());
        textFinder2.setAlsoCheckConsoleOutput(true);
        textFinder2.setAllowForced(true);
        textfinders.add(textFinder2);
        textFinderPublisher.setTextFinders(textfinders);
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.SUCCESS, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.ANOTHER_UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.ANOTHER_UNIQUE_TEXT + "' in console output.", build);
    }

    @Test
    public void twoInRowCanNotGetBetterFirstIsForcing() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject();
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.UNIQUE_TEXT));
        project.getBuildersList().add(new TestEchoBuilder(TestUtils.ANOTHER_UNIQUE_TEXT));
        TextFinderPublisher textFinderPublisher = new TextFinderPublisher(TestUtils.UNIQUE_TEXT);
        List<TextFinder> textfinders = new ArrayList<>();
        TextFinder textFinder1 = new TextFinder(TestUtils.UNIQUE_TEXT);
        textFinder1.setBuildResult(Result.UNSTABLE.toString());
        textFinder1.setAlsoCheckConsoleOutput(true);
        textFinder1.setAllowForced(true);
        textfinders.add(textFinder1);
        TextFinder textFinder2 = new TextFinder(TestUtils.ANOTHER_UNIQUE_TEXT);
        textFinder2.setBuildResult(Result.SUCCESS.toString());
        textFinder2.setAlsoCheckConsoleOutput(true);
        textfinders.add(textFinder2);
        textFinderPublisher.setTextFinders(textfinders);
        project.getPublishersList().add(textFinderPublisher);
        FreeStyleBuild build = rule.buildAndAssertStatus(Result.UNSTABLE, project);
        rule.assertLogContains("[Text Finder] Searching console output...", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.UNIQUE_TEXT + "' in console output.", build);
        rule.assertLogContains(TestUtils.PREFIX + TestUtils.ANOTHER_UNIQUE_TEXT, build);
        rule.assertLogContains(
                "Finished searching for pattern '" + TestUtils.ANOTHER_UNIQUE_TEXT + "' in console output.", build);
    }
}
