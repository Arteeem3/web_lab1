package lab1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.Duration;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import com.fastcgi.FCGIInterface;

public class App {
    private static final String RESPONSE_TEMPLATE = """
            Content-Type: application/json\r
            Content-Length: %d\r
            \r
            %s""";

    private static final Logger log = Logger.getLogger(App.class.getName());

    public static void main(String args[]) {
        log.info("in main function!");
        while (new FCGIInterface().FCGIaccept() >= 0) {
            Instant startTime = Instant.now(); // Start timing

            try {
                // Existing code to handle request
                String method = FCGIInterface.request.params.getProperty("REQUEST_METHOD");
                Map<String, String> params;

                if ("GET".equalsIgnoreCase(method)) {
                    String queryString = FCGIInterface.request.params.getProperty("QUERY_STRING");
                    params = parseParams(queryString);
                } else if ("POST".equalsIgnoreCase(method)) {
                    String contentLengthStr = FCGIInterface.request.params.getProperty("CONTENT_LENGTH");
                    String postDataStr = getPostDataStr(contentLengthStr);
                    params = parseParams(postDataStr);
                } else {
                    sendJson(System.currentTimeMillis(),"{\"error\": \"Unsupported HTTP method\"}");
                    continue;
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
                    sendJson(System.currentTimeMillis(), jsonResponse);
                } else {
                    Instant endTime = Instant.now();
                    long executionTimeMillis = Duration.between(startTime, endTime).toMillis();
                    String jsonResponse = String.format("{\"error\": \"invalid data\", \"executionTime\": %d}", executionTimeMillis);
                    sendJson(System.currentTimeMillis(), jsonResponse);
                }
            } catch (Exception e) {
                Instant endTime = Instant.now();
                long executionTimeMillis = Duration.between(startTime, endTime).toMillis();
                String jsonResponse = String.format("{\"error\": \"%s\", \"executionTime\": %d}", e.getMessage(), executionTimeMillis);
                sendJson(System.currentTimeMillis(), jsonResponse);
            }
        }
    }

    private static String getPostDataStr(String contentLengthStr) throws IOException {
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

    private static void sendJson(long startTime, String jsonDump) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        String currentTimeStr = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(currentTime));

        String responseJson = String.format("{\"response\": %s, \"currentTime\": \"%s\", \"elapsedTime\": %d}",
                jsonDump, currentTimeStr, elapsedTime);

        log.info("in send func");
        log.info(String.format(RESPONSE_TEMPLATE, responseJson.getBytes(StandardCharsets.UTF_8).length, responseJson));
        System.out.println(String.format(RESPONSE_TEMPLATE, responseJson.getBytes(StandardCharsets.UTF_8).length, responseJson));
    }

    private static Map<String, String> parseParams(String data) {
        return Params.parse(data);
    }
}

