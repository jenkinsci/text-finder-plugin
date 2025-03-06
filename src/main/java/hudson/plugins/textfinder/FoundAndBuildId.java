package hudson.plugins.textfinder;

import java.io.Serializable;

final class FoundAndBuildId implements Serializable {
    private static final long serialVersionUID = 1L;
    private final boolean patternFound;
    // this can not be optional, as it can go from slave to master, and optional is not serializable intentionally
    private final String futureBuildId;

    public FoundAndBuildId(boolean patternFound, String futureBuildId) {
        this.patternFound = patternFound;
        this.futureBuildId = futureBuildId;
    }

    public FoundAndBuildId(FoundAndBuildId old, FoundAndBuildId fresh) {
        this(old.patternFound | fresh.patternFound, overwriteByNonNull(old, fresh));
    }

    private static String overwriteByNonNull(FoundAndBuildId old, FoundAndBuildId fresh) {
        return fresh.futureBuildId != null ? fresh.futureBuildId : old.futureBuildId;
    }

    public boolean isPatternFound() {
        return patternFound;
    }

    public String getFutureBuildId() {
        return futureBuildId;
    }
}
