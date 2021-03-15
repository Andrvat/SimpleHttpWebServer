package cmdParser;

import lombok.Builder;

@Builder
public class OptionParamsBuilder {
    private final String opt;
    private final String longOpt;
    private final Boolean hasArg;
    private final String description;

    public String getOpt() {
        return opt;
    }

    public String getLongOpt() {
        return longOpt;
    }

    public Boolean hasArg() {
        return hasArg;
    }

    public String getDescription() {
        return description;
    }
}