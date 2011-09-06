package hudson.plugins.textfinder;

import hudson.model.Result;

import java.util.Arrays;
import java.util.List;

public class TextFinderPublisher_SetsBuildUnstableTest extends AbstractTextFinderPublisherTest {

    public List<TextFinderParameters> givenAListOfTextFinderParameters() {
        return Arrays.asList(new TextFinderParameters(UNSTABLE_DESCRIPTION, FILE_ERROR_THEN_UNSTABLE, UNSTABLE_REGEX_STRING, false, true, false, "UTF-8"));
    }

    @Override
    protected Result thenExpect() {
        return Result.UNSTABLE;
    }
}
