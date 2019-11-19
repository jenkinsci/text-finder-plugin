package hudson.plugins.textfinder;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public final class TextFinderModel extends AbstractDescribableImpl<TextFinderModel>
        implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileSet;
    private final String regexp;
    private boolean succeedIfFound;
    private boolean unstableIfFound;
    private boolean notBuiltIfFound;
    /** True to also scan the whole console output */
    private boolean alsoCheckConsoleOutput;

    @DataBoundConstructor
    public TextFinderModel(String regexp) {
        this.regexp = regexp;
        // Attempt to compile regular expression
        try {
            Pattern.compile(regexp);
        } catch (PatternSyntaxException e) {
            // falls through
        }
    }

    @DataBoundSetter
    public void setFileSet(String fileSet) {
        if (fileSet == null) {
            this.fileSet = null;
        } else {
            if (fileSet.trim().isEmpty()) {
                this.fileSet = null;
            } else {
                this.fileSet = fileSet.trim();
            }
        }
    }

    @DataBoundSetter
    public void setSucceedIfFound(boolean succeedIfFound) {
        this.succeedIfFound = succeedIfFound;
    }

    @DataBoundSetter
    public void setUnstableIfFound(boolean unstableIfFound) {
        this.unstableIfFound = unstableIfFound;
    }

    @DataBoundSetter
    public void setNotBuiltIfFound(boolean notBuiltIfFound) {
        this.notBuiltIfFound = notBuiltIfFound;
    }

    @DataBoundSetter
    public void setAlsoCheckConsoleOutput(boolean alsoCheckConsoleOutput) {
        this.alsoCheckConsoleOutput = alsoCheckConsoleOutput;
    }

    @Deprecated
    private TextFinderModel(
            String fileSet,
            String regexp,
            boolean succeedIfFound,
            boolean unstableIfFound,
            boolean alsoCheckConsoleOutput,
            boolean notBuiltIfFound) {
        this.fileSet = fileSet != null ? Util.fixEmpty(fileSet.trim()) : null;
        this.regexp = regexp;
        this.succeedIfFound = succeedIfFound;
        this.unstableIfFound = unstableIfFound;
        this.alsoCheckConsoleOutput = alsoCheckConsoleOutput;
        this.notBuiltIfFound = notBuiltIfFound;

        // Attempt to compile regular expression
        try {
            Pattern.compile(regexp);
        } catch (PatternSyntaxException e) {
            // falls through
        }
    }

    public String getFileSet() {
        return fileSet;
    }

    public String getRegexp() {
        return regexp;
    }

    public boolean isSucceedIfFound() {
        return succeedIfFound;
    }

    public boolean isUnstableIfFound() {
        return unstableIfFound;
    }

    public boolean isNotBuiltIfFound() {
        return notBuiltIfFound;
    }

    public boolean isAlsoCheckConsoleOutput() {
        return alsoCheckConsoleOutput;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<TextFinderModel> {
        @Override
        public String getDisplayName() {
            return "Text Finder";
        }
    }
}
