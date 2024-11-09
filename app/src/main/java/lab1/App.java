package lab1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.time.Instant;
import java.time.Duration;
import com.fastcgi.FCGIInterface;

// Интерфейс обработчика запроса
interface RequestHandler {
    void handleRequest() throws IOException;
}

// Реальный обработчик запроса
class HandlerProxy implements RequestHandler {
    private Instant startTime;

    public HandlerProxy() {
        this.startTime = Instant.now();
    }

    @Override
    public void handleRequest() throws IOException {
        String method = FCGIInterface.request.params.getProperty("REQUEST_METHOD");
        Map<String, String> params;

        if ("POST".equalsIgnoreCase(method)) {
            String contentLengthStr = FCGIInterface.request.params.getProperty("CONTENT_LENGTH");
            String postDataStr = getPostDataStr(contentLengthStr);
            params = parseParams(postDataStr);
        } else {
            sendJson("{\"error\": \"Unsupported HTTP method\"}");
            return;
        }

        // Parse and validate parameters
        int x = Integer.parseInt(params.get("x"));
        float y = Float.parseFloat(params.get("y"));
        float r = Float.parseFloat(params.get("r"));

        if (Validator.validateX(x) && Validator.validateY(y) && Validator.validateR(r)) {
            boolean result = GeometryChecker.hit(x, y, r);
            Instant endTime = Instant.now(); // End timing
            long executionTimeMillis = Duration.between(startTime, endTime).toMillis();

            // Include execution time in response
            String jsonResponse = String.format("{\"result\": %b, \"executionTime\": %d}", result, executionTimeMillis);
            sendJson(jsonResponse);
        } else {
            Instant endTime = Instant.now();
            long executionTimeMillis = Duration.between(startTime, endTime).toMillis();
            String jsonResponse = String.format("{\"error\": \"invalid data\", \"executionTime\": %d}", executionTimeMillis);
            sendJson(jsonResponse);
        }
    }

    private String getPostDataStr(String contentLengthStr) throws IOException {
        int contentLength = contentLengthStr != null ? Integer.parseInt(contentLengthStr) : 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        char[] postData = new char[contentLength];
        int totalRead = 0;
        while (totalRead < contentLength) {
            int read = reader.read(postData, totalRead, contentLength - totalRead);
            if (read == -1) {
                break;
            }
            totalRead += read;
        }
        return new String(postData, 0, totalRead);
    }

    private void sendJson(String jsonDump) {
        System.out.printf((App.RESPONSE_TEMPLATE) + "%n", jsonDump.getBytes(StandardCharsets.UTF_8).length, jsonDump);
    }

    private Map<String, String> parseParams(String data) {
        return Params.parse(data);
    }
}

// Прокси-класс для проверки метода запроса
class MethodCheckProxy implements RequestHandler {
    private HandlerProxy HandlerProxy;

    public MethodCheckProxy() {
        this.HandlerProxy = new HandlerProxy();
    }

    @Override
    public void handleRequest() throws IOException {
        String method = FCGIInterface.request.params.getProperty("REQUEST_METHOD");
        if (!"POST".equalsIgnoreCase(method)) {
            sendMethodNotAllowedError();
        } else {
            HandlerProxy.handleRequest();
        }
    }

    private void sendMethodNotAllowedError() {
        String jsonResponse = "{\"error\": \"Method Not Allowed\"}";
        System.out.printf((App.RESPONSE_TEMPLATE) + "%n", jsonResponse.getBytes(StandardCharsets.UTF_8).length, jsonResponse);
    }
}

// Основной класс приложения с запуском
public class App {
    public static final String RESPONSE_TEMPLATE = """
            Content-Type: application/json\r
            Content-Length: %d\r
            \r
            %s""";

    public static void main(String[] args) {
        while (new FCGIInterface().FCGIaccept() >= 0) {
            try {
                // Используем прокси для обработки запросов
                RequestHandler handler = new MethodCheckProxy();
                handler.handleRequest();
            } catch (IOException e) {
                String jsonResponse = String.format("{\"error\": \"%s\"}", e.getMessage());
                System.out.printf((RESPONSE_TEMPLATE) + "%n", jsonResponse.getBytes(StandardCharsets.UTF_8).length, jsonResponse);
            }
        }
    }
}
