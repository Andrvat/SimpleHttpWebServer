package httpServer;

import java.io.IOException;

@FunctionalInterface
public interface Handlesable {
    void handleRequest() throws IOException;
}
