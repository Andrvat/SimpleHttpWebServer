package cmdParser;

import org.apache.commons.cli.ParseException;

@FunctionalInterface
public interface Parseable {
    void parseArgs(String[] args) throws ParseException;
}
