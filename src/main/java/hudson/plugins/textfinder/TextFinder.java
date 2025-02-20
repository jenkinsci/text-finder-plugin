package hudson.plugins.textfinder;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

public class TextFinder extends AbstractDescribableImpl<TextFinder> implements Serializable {

    @NonNull
    private /* final */ String regexp;

    @CheckForNull
    private String fileSet;

    @NonNull
    private String buildResult = Result.FAILURE.toString();

    private TextFinderChangeCondition changeCondition = TextFinderChangeCondition.MATCH_FOUND;
    private boolean alsoCheckConsoleOutput;
    private boolean allowForced;

    @Restricted(NoExternalUse.class)
    public String getRegexp() {
        return regexp;
    }

    @DataBoundConstructor
    @Restricted(NoExternalUse.class)
    public TextFinder(String regexp) {
        this.regexp = Objects.requireNonNull(regexp);
    }

    /** This is gross, but fortunately it is only used in a deprecated code path. */
    @Restricted(NoExternalUse.class)
    void setRegexp(String regexp) {
        this.regexp = Objects.requireNonNull(regexp);
    }

    @Restricted(NoExternalUse.class)
    public String getFileSet() {
        return fileSet;
    }

    @DataBoundSetter
    @Restricted(NoExternalUse.class)
    public void setFileSet(String fileSet) {
        this.fileSet = fileSet != null ? Util.fixEmpty(fileSet.trim()) : null;
    }

    @Restricted(NoExternalUse.class)
    public String getBuildResult() {
        return buildResult;
    }

    @DataBoundSetter
    @Restricted(NoExternalUse.class)
    public void setBuildResult(String buildResult) {
        if (buildResult == null || Util.fixEmpty(buildResult.trim()) == null) {
            buildResult = Result.FAILURE.toString();
        }

        if (!buildResult.equalsIgnoreCase(Result.fromString(buildResult).toString())) {
            throw new IllegalArgumentException("buildResult is invalid: "
                    + buildResult
                    + ". Valid options are SUCCESS, UNSTABLE, FAILURE, NOT_BUILT and"
                    + " ABORTED.");
        }

        this.buildResult = buildResult;
    }

    @Restricted(NoExternalUse.class)
    public TextFinderChangeCondition getChangeCondition() {
        return changeCondition;
    }

    @DataBoundSetter
    @Restricted(NoExternalUse.class)
    public void setChangeCondition(TextFinderChangeCondition changeCondition) {
        this.changeCondition = changeCondition;
    }

    @Restricted(NoExternalUse.class)
    public boolean isAlsoCheckConsoleOutput() {
        return alsoCheckConsoleOutput;
    }

    @DataBoundSetter
    @Restricted(NoExternalUse.class)
    public void setAlsoCheckConsoleOutput(boolean alsoCheckConsoleOutput) {
        this.alsoCheckConsoleOutput = alsoCheckConsoleOutput;
    }

    @Restricted(NoExternalUse.class)
    public boolean isAllowForced() {
        return allowForced;
    }

    @DataBoundSetter
    @Restricted(NoExternalUse.class)
    public void setAllowForced(boolean allowForced) {
        this.allowForced = allowForced;
    }

    /**
     * Called by XStream after object construction
     *
     * @return modified object
     */
    protected Object readResolve() {
        if (changeCondition == null) {
            changeCondition = TextFinderChangeCondition.MATCH_FOUND;
        }

        return this;
    }

    @Symbol("textFinder")
    @Extension
    public static class DescriptorImpl extends Descriptor<TextFinder> {
        @NonNull
        public String getDisplayName() {
            // This descriptor is not intended to be displayed on its own.
            return "";
        }

        /**
         * Checks the regular expression validity.
         *
         * @param value The expression to check
         * @return The form validation result
         */
        @SuppressWarnings({"lgtm[jenkins/csrf]", "lgtm[jenkins/no-permission-check]"})
        public FormValidation doCheckRegexp(@QueryParameter String value) {
            if (Util.fixEmptyAndTrim(value) == null) {
                // not entered yet
                return FormValidation.ok();
            }

            try {
                Pattern.compile(value);
                return FormValidation.ok();
            } catch (PatternSyntaxException e) {
                return FormValidation.error(e.getMessage());
            }
        }

        @SuppressWarnings({"lgtm[jenkins/csrf]", "lgtm[jenkins/no-permission-check]"})
        public ListBoxModel doFillBuildResultItems() {
            ListBoxModel r = new ListBoxModel();
            for (Result result :
                    Arrays.asList(Result.SUCCESS, Result.UNSTABLE, Result.FAILURE, Result.NOT_BUILT, Result.ABORTED)) {
                r.add(result.toString());
            }
            return r;
        }
    }

    private static final long serialVersionUID = 1L;
}
