package httpServer;

import lombok.Builder;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Builder
public class HttpRequestsHandler implements Handler {
    private static final Logger logger = Logger.getLogger(ServerLauncher.class.getName());

    private final int clientId;

    private InputStream inputStream;
    private OutputStream outputStream;
    private final String serverDirectory;
    private final Socket clientSocket;

    private String requestLine;
    private final Map<String, String> requestHeaderLines = new LinkedHashMap<>();

    private String statusLine;
    private final Map<String, String> answerHeaderLines = new LinkedHashMap<>();
    private byte[] answerEntityBody;

    @Override
    public void handleRequest() throws IOException, EmptyHttpRequestException {
        inputStream = clientSocket.getInputStream();
        outputStream = clientSocket.getOutputStream();

        do {
            if (clientSocket.isClosed()) {
                break;
            }


            readHttpRequestFromInputStream();
            logger.log(Level.INFO, "Client #" + clientId + ": " +
                    "The request from the input stream was successfully read by the server");

            String statusText;
            String contentType;

            String requestMethod = getRequestMethod();
            logger.log(Level.INFO, "Client #" + clientId + ": " +
                    "The request method was successfully got by the server");

            if (!HttpUtils.getSupportedMethods().contains(requestMethod)) {
                prepareAndSendServerAnswerToClient("Method Not Allowed",
                        "text",
                        "Method Not Allowed".getBytes(StandardCharsets.UTF_8),
                        null);
                return;
            }

            if (!isThereAtLeastOneDataTypeSupportedByServer()) {
                prepareAndSendServerAnswerToClient("Not Acceptable",
                        "text",
                        HttpUtils.getContentTypes().toString().getBytes(StandardCharsets.UTF_8),
                        null);
                return;
            }

            String requestUrl = getRequestUrl();
            Path requestedResourcePath = Path.of(this.serverDirectory + requestUrl);

            if (!Files.exists(requestedResourcePath) || Files.isDirectory(requestedResourcePath)) {
                prepareAndSendServerAnswerToClient("Not Found",
                        "text",
                        "Not Found".getBytes(StandardCharsets.UTF_8),
                        null);
                return;
            }

            prepareAndSendServerAnswerToClient("OK",
                    HttpUtils.getContentTypes().get(getFileExtension(requestedResourcePath)),
                    Files.readAllBytes(requestedResourcePath),
                    requestedResourcePath);
        } while (requestHeaderLines.containsKey("Connection") &&
                requestHeaderLines.get("Connection").equals("keep-alive"));

        logger.log(Level.INFO, "Client #" + clientId + ": " +
                "Finish of processing all client's requests");
    }

    private void prepareAndSendServerAnswerToClient(String statusText, String contentType,
                                                    byte[] entityBody, Path requestedResourcePath) throws IOException {
        answerEntityBody = entityBody;
        sendAnswerToClient(statusText, contentType, requestedResourcePath);
    }

    private void readHttpRequestFromInputStream() throws IOException, EmptyHttpRequestException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        requestLine = reader.readLine();
        if (requestLine == null) {
            throw new EmptyHttpRequestException("Couldn't read request line from client socket");
        }
        String currentLineFromHttpRequest = reader.readLine();
        while (currentLineFromHttpRequest != null && !currentLineFromHttpRequest.equals("")) {
            requestHeaderLines.put(
                    toFirstUpperCase(currentLineFromHttpRequest.substring(0, currentLineFromHttpRequest.indexOf(":"))),
                    currentLineFromHttpRequest.substring(currentLineFromHttpRequest.indexOf(" ") + 1));
            currentLineFromHttpRequest = reader.readLine();
        }
    }

    private String toFirstUpperCase(String word) {
        if (word == null || word.isEmpty()) {
            return "";
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private boolean isThereAtLeastOneDataTypeSupportedByServer() {
        String acceptHeader = requestHeaderLines.get("Accept");
        List<String> acceptedTypes = new ArrayList<>(Arrays.asList(acceptHeader.split(",")));

        if (acceptedTypes.contains("*/*")) {
            return true;
        }

        for (String type : acceptedTypes) {
            if (type.contains("*")) {
                String mimeType = type.substring(0, type.indexOf("/"));
                for (Map.Entry<String, String> entry : HttpUtils.getContentTypes().entrySet()) {
                    String value = entry.getValue();
                    if (value.substring(0, value.indexOf("/")).equals(mimeType)) {
                        return true;
                    }
                }
            }

            if (HttpUtils.getContentTypes().containsValue(type)) {
                return true;
            }
        }

        return false;

    }

    private String getRequestMethod() {
        return requestLine.split(" ")[0];
    }

    private String getRequestUrl() {
        return requestLine.split(" ")[1];
    }

    private String getFileExtension(Path filePath) {
        String name = filePath.getFileName().toString();
        int extensionStartIndex = name.lastIndexOf(".");
        return extensionStartIndex == -1 ? "" : name.substring(extensionStartIndex + 1);
    }

    private void sendAnswerToClient(String statusText, String type, Path requestedResourcePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("HTTP/1.1")
                .append(" ")
                .append(HttpUtils.getHeadersCodes().get(statusText))
                .append(" ")
                .append(statusText)
                .append("\n");
        statusLine = stringBuilder.toString();
        stringBuilder.setLength(0);
        writer.write(statusLine);
        logger.log(Level.INFO, "Client #" + clientId + ": " +
                "Status line was successfully written");

        if (HttpUtils.getHeadersCodes().get(statusText) == 200) {
            createAnswerHeaderLines(type, requestedResourcePath);
            for (Map.Entry<String, String> entry : answerHeaderLines.entrySet()) {
                stringBuilder.append(entry.getKey())
                        .append(":")
                        .append(" ")
                        .append(entry.getValue())
                        .append("\n");

                writer.write(stringBuilder.toString());
                stringBuilder.setLength(0);
            }
        }
        logger.log(Level.INFO, "Client #" + clientId + ": " +
                "All headers was successfully written");

        writer.write("\n");
        writer.flush();

        outputStream.write(answerEntityBody);
        logger.log(Level.INFO, "Client #" + clientId + ": " +
                "Data was successfully written. Output stream will close");

        outputStream.close();
    }

    private void createAnswerHeaderLines(String type, Path requestedResourcePath) {
        answerHeaderLines.put("Content-Type", type);
        answerHeaderLines.put("Content-Length", Integer.toString(answerEntityBody.length));
        answerHeaderLines.put("Date", new Date().toString());
        answerHeaderLines.put("Last-Modified", new Date(new File(requestedResourcePath.toString()).lastModified()).toString());
        answerHeaderLines.put("Server", "Andrvat localhost");
    }

}
