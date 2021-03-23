package cmdParser;

import org.apache.commons.cli.ParseException;

public interface Parser {
    void parseArgs(String[] args) throws ParseException;
}
