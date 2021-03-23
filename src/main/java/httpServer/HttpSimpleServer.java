package httpServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpSimpleServer implements Runnable {
    private static final Logger logger = Logger.getLogger(HttpSimpleServer.class.getName());

    private final Integer clientPort;

    private final String serverDirectory;

    public HttpSimpleServer(Integer clientPort, String serverDirectory) {
        this.clientPort = clientPort;
        this.serverDirectory = serverDirectory;
    }

    @Override
    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(clientPort);
            logger.log(Level.INFO, "Server started on port #" + serverSocket.getLocalPort());
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "Port " + clientPort + " is not available", exception);
            return;
        }

        int clientsCounter = 0;
        while (true) {
            try {
                if (System.in.available() > 0) {
                    if (System.in.read() == 'q') {
                        serverSocket.close();
                        logger.log(Level.INFO, "Server socket was closed. Server successfully stopped...");
                        break;
                    }
                }
            } catch (IOException exception) {
                logger.log(Level.SEVERE, "Something went wrong with server turning off ...", exception);
                logger.log(Level.SEVERE, "Server was crash-stopped...");
                continue;
            }

            try (Socket clientSocket = serverSocket.accept()) {
                logger.log(Level.INFO, "Client socket connection established");

                HttpRequestsHandler requestsHandler = HttpRequestsHandler.builder()
                        .clientId(clientsCounter)
                        .serverDirectory(serverDirectory)
                        .clientSocket(clientSocket)
                        .build();

                requestsHandler.handleRequest();
                clientsCounter++;
                logger.log(Level.INFO, "Client request handled. " +
                        "Total clients: " + clientsCounter);

            } catch (IOException exception) {
                logger.log(Level.SEVERE, "Something went wrong with socket...", exception);
            }
        }
    }
}
