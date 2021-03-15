package httpServer;

import lombok.Builder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Builder
public class HttpRequestsHandler implements Handlesable {
    private static final Logger logger = Logger.getLogger(ServerLauncher.class.getName());

    private static final ArrayList<String> SUPPORTED_METHODS = new ArrayList<>() {{
        add("GET");
    }};

    private static final Map<String, String> CONTENT_TYPES = new LinkedHashMap<>() {{
        put("txt", "text/plain");
        put("html", "text/html");
        put("jpg", "image/jpeg");
        put("", "text/plain");
    }};

    private static final Map<String, Integer> HEADERS_CODES = new LinkedHashMap<>() {{
        put("Not Found", 404);
        put("Method Not Allowed", 405);
        put("OK", 200);
        put("Not Acceptable", 406);
    }};

    private final int clientId;

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final String serverDirectory;

    private String requestLine;
    private final Map<String, String> requestHeaderLines = new LinkedHashMap<>();

    private String statusLine;
    private final Map<String, String> answerHeaderLines = new LinkedHashMap<>();
    private byte[] answerEntityBody;

    public boolean isConnectionPersistent() {
        if (requestHeaderLines.containsKey("Connection")) {
            return requestHeaderLines.get("Connection").equals("keep-alive");
        }
        return false;
    }

    @Override
    public void handleRequest() throws IOException {
        readHttpRequestFromInputStream();
        logger.log(Level.INFO, "Client #" + clientId + ": " +
                "The request from the input stream was successfully read by the server");

        String statusText;
        String contentType;

        String requestMethod = getRequestMethod();
        logger.log(Level.INFO, "Client #" + clientId + ": " +
                "The request method was successfully got by the server");

        if (!SUPPORTED_METHODS.contains(requestMethod)) {
            statusText = "Method Not Allowed";
            contentType = "text";
            answerEntityBody = statusText.getBytes(StandardCharsets.UTF_8);
            sendAnswerToClient(statusText, contentType, null);
            return;
        }

        if (!isThereAtLeastOneDataTypeSupportedByServer()) {
            statusText = "Not Acceptable";
            contentType = "text";
            answerEntityBody = CONTENT_TYPES.toString().getBytes(StandardCharsets.UTF_8);
            sendAnswerToClient(statusText, contentType, null);
            return;
        }

        String requestUrl = getRequestUrl();
        Path requestedResourcePath = Path.of(this.serverDirectory + requestUrl);

        if (!Files.exists(requestedResourcePath) || Files.isDirectory(requestedResourcePath)) {
            statusText = "Not Found";
            contentType = "text";
            answerEntityBody = statusText.getBytes(StandardCharsets.UTF_8);
            sendAnswerToClient(statusText, contentType, null);
            return;
        }

        statusText = "OK";
        contentType = CONTENT_TYPES.get(getFileExtension(requestedResourcePath));
        answerEntityBody = Files.readAllBytes(requestedResourcePath);
        sendAnswerToClient(statusText, contentType, requestedResourcePath);
    }

    private void readHttpRequestFromInputStream() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        requestLine = reader.readLine();
        String currentLineFromHttpRequest;
        while (!(currentLineFromHttpRequest = reader.readLine()).equals("")) {
            requestHeaderLines.put(currentLineFromHttpRequest.substring(0, currentLineFromHttpRequest.indexOf(":")),
                    currentLineFromHttpRequest.substring(currentLineFromHttpRequest.indexOf(" ") + 1));
        }
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
                for (Map.Entry<String, String> entry : CONTENT_TYPES.entrySet()) {
                    String value = entry.getValue();
                    if (value.substring(0, value.indexOf("/")).equals(mimeType)) {
                        return true;
                    }
                }
            }

            if (CONTENT_TYPES.containsValue(type)) {
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
                .append(HEADERS_CODES.get(statusText))
                .append(" ")
                .append(statusText)
                .append("\n");
        statusLine = stringBuilder.toString();
        stringBuilder.setLength(0);
        writer.write(statusLine);
        logger.log(Level.INFO, "Client #" + clientId + ": " +
                "Status line was successfully written");

        if (HEADERS_CODES.get(statusText) == 200) {
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
