package cmdParser;

import org.apache.commons.cli.*;

import java.util.logging.Logger;

public class CommandLineArgsParser implements Parseable {
    private static final Logger logger = Logger.getLogger(CommandLineArgsParser.class.getName());

    private final Options parserOptions;
    private Integer userPort;

    public CommandLineArgsParser() {
        parserOptions = new Options();
        parserOptions.addOption(createNewOption(OptionParamsBuilder.builder()
                .opt("p")
                .longOpt("port")
                .hasArg(true)
                .description("The port where the connection will start")
                .build()));

        logger.info("All options were added by class constructor");
    }

    private Option createNewOption(OptionParamsBuilder paramsBuilder) {
        Option option = new Option(paramsBuilder.getOpt(),
                paramsBuilder.getLongOpt(),
                paramsBuilder.hasArg(),
                paramsBuilder.getDescription());
        option.setRequired(true);

        logger.info("Option was created with setRequired(false)");
        return option;
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
