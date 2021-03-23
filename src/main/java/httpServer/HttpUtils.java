package httpServer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpUtils {
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

    public static ArrayList<String> getSupportedMethods() {
        return SUPPORTED_METHODS;
    }

    public static Map<String, String> getContentTypes() {
        return CONTENT_TYPES;
    }

    public static Map<String, Integer> getHeadersCodes() {
        return HEADERS_CODES;
    }
}
