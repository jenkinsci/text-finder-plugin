package hudson.plugins.textfinder;

import hudson.util.EnumConverter;

import org.kohsuke.stapler.Stapler;

public enum TextFinderChangeCondition {
    MATCH_FOUND("Change the build result if a match is found"),
    MATCH_NOT_FOUND("Change the build result if a match is not found");

    private final String description;

    TextFinderChangeCondition(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    static {
        // Allow conversion from a string to an enumeration in the databinding process.
        Stapler.CONVERT_UTILS.register(new EnumConverter(), TextFinderChangeCondition.class);
    }
}
