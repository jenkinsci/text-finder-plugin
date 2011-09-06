package hudson.plugins.textfinder;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;

@ExportedBean
public class TextFinderParameters extends AbstractDescribableImpl<TextFinderParameters> implements Serializable {

    private final String description;
    private final String fileSet;
    private final String regexp;
    private final boolean succeedIfFound;
    private final boolean unstableIfFound;
    private final boolean alsoCheckConsoleOutput;
    private final String encoding;

    @DataBoundConstructor
    public TextFinderParameters(String description, String fileSet, String regexp, boolean succeedIfFound, boolean unstableIfFound, boolean alsoCheckConsoleOutput, String encoding) {
        this.description = description;
        this.fileSet = Util.fixEmpty(fileSet.trim());
        this.regexp = regexp;
        this.succeedIfFound = succeedIfFound;
        this.unstableIfFound = unstableIfFound;
        this.alsoCheckConsoleOutput = alsoCheckConsoleOutput;
        this.encoding = encoding == null ? "UTF-8" : encoding;
    }

    @Exported
    public String getDescription() {
        return description;
    }

    @Exported
    public String getFileSet() {
        return fileSet;
    }

    @Exported
    public String getRegexp() {
        return regexp;
    }

    @Exported
    public boolean isSucceedIfFound() {
        return succeedIfFound;
    }

    @Exported
    public boolean isUnstableIfFound() {
        return unstableIfFound;
    }

    @Exported
    public boolean isAlsoCheckConsoleOutput() {
        return alsoCheckConsoleOutput;
    }

    @Exported
    public String getEncoding() {
        return encoding;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<TextFinderParameters> {

        public DescriptorImpl() {
            super(TextFinderParameters.class);
        }

        //        public FormValidation doCheckUrl(@QueryParameter String value) {
//            if (value == null || value.isEmpty()) {
//                return FormValidation.error("Please enter TextFinder parameters.");
//            } else {
//                return FormValidation.ok();
//            }
//        }

        @Override
        public String getDisplayName() {
            return "";
        }
    }
}
