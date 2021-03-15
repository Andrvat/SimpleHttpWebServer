package httpServer;

import cmdParser.CommandLineArgsParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerLauncher {
    private static final Logger logger = Logger.getLogger(ServerLauncher.class.getName());

    private static final Integer DEFAULT_PORT = 8080;

    public static void main(String[] args) {

        CommandLineArgsParser commandLineArgsParser = new CommandLineArgsParser();
        try {
            commandLineArgsParser.parseArgs(args);
        } catch (ParseException exception) {
            logger.log(Level.SEVERE, "Couldn't parse command line arguments", exception);
            HelpFormatter formatter = new HelpFormatter();
            System.out.println(exception.getMessage());
            formatter.printHelp(ServerLauncher.class.getName(), commandLineArgsParser.getParserOptions());
            logger.log(Level.SEVERE, "Program stopped");
            return;
        }

        Integer workPort = commandLineArgsParser.isUserPortRequired() ? commandLineArgsParser.getUserPort() : DEFAULT_PORT;
        HttpSimpleServer server = HttpSimpleServer.builder()
                .clientPort(workPort)
                .serverDirectory(System.getProperty("user.dir"))
                .build();

        server.run();

    }
}
