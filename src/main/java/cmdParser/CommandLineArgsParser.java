package cmdParser;

import org.apache.commons.cli.*;

import javax.swing.text.StyledEditorKit;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class CommandLineArgsParser implements Parser {
    private static final Logger logger = Logger.getLogger(CommandLineArgsParser.class.getName());

    private final Options parserOptions;
    private Integer userPort;

    public CommandLineArgsParser() throws IOException {
        parserOptions = new Options();

        Properties serverPortProperties = new Properties();
        serverPortProperties.load(new FileInputStream("src/main/resources/serverMainPortOption.properties"));
        Option serverPortOption = new Option(
                serverPortProperties.getProperty("opt"),
                serverPortProperties.getProperty("longOpt"),
                Boolean.parseBoolean(serverPortProperties.getProperty("hasArg")),
                serverPortProperties.getProperty("description"));
        serverPortOption.setRequired(true);
        parserOptions.addOption(serverPortOption);

        logger.info("All options were added by class constructor");
    }

    @Override
    public void parseArgs(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(parserOptions, args);

        String userPortInStringFormat = commandLine.getOptionValue("p");
        if (isUserPortRequired() && userPortInStringFormat != null) {
            userPort = Integer.parseInt(userPortInStringFormat);
        }
        logger.info("User port: " + userPort);

        logger.info("All command line arguments were read");
    }

    public Options getParserOptions() {
        return parserOptions;
    }

    public Integer getUserPort() {
        return userPort;
    }

    public Boolean isUserPortRequired() {
        return parserOptions.getOption("p").isRequired();
    }
}
