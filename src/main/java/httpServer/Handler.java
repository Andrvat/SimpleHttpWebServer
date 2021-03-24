package httpServer;

import java.io.IOException;

public interface Handler {
    void handleRequest() throws IOException, EmptyHttpRequestException;
}
