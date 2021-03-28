package httpServer;

public class EmptyHttpRequestException extends Exception {
    public EmptyHttpRequestException(String message) {
        super(message);
    }

    public EmptyHttpRequestException() {
        super();
    }

    public EmptyHttpRequestException(Throwable t) {
        super(t);
    }

    public EmptyHttpRequestException(String message, Throwable t) {
        super(message, t);
    }
}
