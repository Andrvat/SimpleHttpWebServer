package httpServer;

import cmdParser.CommandLineArgsParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerLauncher {
    private static final Logger logger = Logger.getLogger(ServerLauncher.class.getName());

    private static final Integer DEFAULT_PORT = 8080;

    public static void main(String[] args) {

        CommandLineArgsParser commandLineArgsParser;
        try {
            commandLineArgsParser = new CommandLineArgsParser();
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "Couldn't find command line properties", exception);
            return;
        }
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
        HttpSimpleServer server = new HttpSimpleServer(workPort, System.getProperty("user.dir"));

        server.run();

    }
}
