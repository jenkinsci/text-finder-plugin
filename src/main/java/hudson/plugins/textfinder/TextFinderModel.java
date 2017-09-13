package hudson.plugins.textfinder;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class TextFinderModel extends AbstractDescribableImpl<TextFinderModel> implements Serializable {
    private static final long serialVersionUID = 7645849785536280615L;

    public final String fileSet;
    public final String regexp;
    public final boolean succeedIfFound;
    public final boolean unstableIfFound;
    public final boolean notBuiltIfFound;
    /**
     * True to also scan the whole console output
     */
    public final boolean alsoCheckConsoleOutput;

    @DataBoundConstructor
    public TextFinderModel(String fileSet, String regexp, boolean succeedIfFound, boolean unstableIfFound, boolean alsoCheckConsoleOutput, boolean notBuiltIfFound) {
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
            return "Text finder";
        }
    }
}
