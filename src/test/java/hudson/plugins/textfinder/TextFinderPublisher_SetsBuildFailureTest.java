package hudson.plugins.textfinder;

import hudson.model.Result;

import java.util.Arrays;
import java.util.List;

public class TextFinderPublisher_SetsBuildFailureTest extends AbstractTextFinderPublisherTest {

    public List<TextFinderParameters> givenAListOfTextFinderParameters() {
        return Arrays.asList(new TextFinderParameters(ERROR_DESCRIPTION, FILE_ERROR_THEN_UNSTABLE, ERROR_REGEX_STRING, false, false, false, "UTF-8"));
    }

    @Override
    protected Result thenExpect() {
        return Result.FAILURE;
    }
}
