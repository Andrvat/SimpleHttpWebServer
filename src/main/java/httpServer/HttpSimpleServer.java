package httpServer;

import lombok.Builder;
import lombok.SneakyThrows;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

@Builder
public class HttpSimpleServer implements Runnable {
    private static final Logger logger = Logger.getLogger(HttpSimpleServer.class.getName());

    private final Integer clientPort;

    private final String serverDirectory;

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
                Socket clientSocket = serverSocket.accept();
                logger.log(Level.INFO, "Client socket connection established");

                HttpRequestsHandler requestsHandler = HttpRequestsHandler.builder()
                        .clientId(clientsCounter)
                        .inputStream(clientSocket.getInputStream())
                        .outputStream(clientSocket.getOutputStream())
                        .serverDirectory(serverDirectory)
                        .build();

                requestsHandler.handleRequest();
                clientsCounter++;
                logger.log(Level.INFO, "Client request handled. " +
                        "Total clients: " + clientsCounter);

                if (!requestsHandler.isConnectionPersistent()) {
                    clientSocket.close();
                    logger.log(Level.INFO, "Last client socket was closed");
                }
            } catch (IOException exception) {
                logger.log(Level.SEVERE, "Something went wrong with socket...", exception);
                return;
            }

        }

    }
}
