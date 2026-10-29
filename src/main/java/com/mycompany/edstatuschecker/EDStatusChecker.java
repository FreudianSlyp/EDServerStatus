
package com.mycompany.edstatuschecker;

/**
 * https://ed-server-status.orerve.net/ {"status": "Maintenance", "message": "", "code": 0, "product": "FORC-FDEV-DO-1000"}
 *
 * Perhaps provide the following as parameters to the application
 * URL to check
 * Delay between checks
 * Key to find/examine
 * What to do on a change of Key value
 * Debug on/off
 *
 */
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EDStatusChecker {

    static String s_lastStatus = "";
    static String s_serverURL = "https://ed-server-status.orerve.net";
    static int s_delayTimeSecs = 60;
    static String s_searchKey = "status";

    static boolean DEBUG = false;

    public static void main(String[] args) {

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ObjectMapper serverStatus = new ObjectMapper();

        HttpRequest[] request = new HttpRequest[1];
        HttpClient client = HttpClient.newHttpClient();

        request[0] = HttpRequest.newBuilder()
                .uri(URI.create(s_serverURL))
                .GET()
                .build();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // using disposable variable here - i'm never going to use it
        ScheduledFuture<?> _ = scheduler.scheduleAtFixedRate(() -> {

            HttpResponse<String> response = new HttpBlankResponse<>();

            try {

                response = client.send(request[0], HttpResponse.BodyHandlers.ofString());

                int responseCode = response.statusCode();

                // check the response codes, if not 'ok' (200-299) then determine what the problem was and report it
                // this list is not exhaustive and generalises the responses for ease of use
                // For more information consult : https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
                if (!(responseCode >= 200 && responseCode <= 299)) {
                    // ** IMPORTANT ** openjdk doesnt yet support range based switch statements .. booo!
                    // ** IMPORTANT ** so you'll need to use java --enable-preview -jar <jarname here> 
                    String responseMessage = switch (responseCode) {

                        case Integer s when s >= 100 && s <= 199 ->
                            "Information Only";
                        case Integer s when s >= 300 && s <= 399 ->
                            "Redirect";
                        case 400 ->
                            "Bad Request";
                        case 401 ->
                            "Unauthorized";
                        case 403 ->
                            "Forbidden";
                        case 404 ->
                            "Page not found";
                        case Integer s when s >= 405 && s <= 499 ->
                            "Client side error";
                        case Integer s when s >= 500 && s <= 599 ->
                            "Server error";
                        default ->
                            "Unknown";
                    };

                    System.err.println(String.format("%s : A problem occurred whilst trying to query the server %s. status code %d, message %s", LocalDateTime.now().format(formatter), s_serverURL, responseCode, responseMessage));

                    // set lastStatus to indicate we had a proble and that we should bleat about it
                    s_lastStatus = "Problem Occurred";

                } else {

                    Map<String, Object> map = serverStatus.readValue(response.body(), new TypeReference<Map<String, Object>>() {
                    });

                    if (map.containsKey(s_searchKey)) {

                        String currentStatus = map.get(s_searchKey).toString();
                        if (!currentStatus.equals(s_lastStatus)) {
                            System.out.println(String.format("%s : \"%s\" has changed, it's now : %s", LocalDateTime.now().format(formatter), s_searchKey, currentStatus));

                            // Reset the lastStatus so we get new failiure reports if they happen after a successful connection
                            s_lastStatus = currentStatus;
                        }

                    } else {

                        System.out.println(String.format("%s : Key \"$s\" not found in returned data", LocalDateTime.now().format(formatter), s_searchKey));
                    }
                }

            } catch (Exception ex) {

                // if the lastStatus is a problem, we dont need to alert on it again
                if (!s_lastStatus.equals("Problem Occurred")) {
                    System.err.println(String.format("%s : A problem occurred whilst trying to query the server %s. message %s", LocalDateTime.now().format(formatter), s_serverURL, ex.getMessage()));
                }

                // set the last status message so it gets reported and updated
                s_lastStatus = "Problem Occurred";
                if (DEBUG) {
                    System.err.println(response);
                }

            }

            response = null;

        }, 0, s_delayTimeSecs, TimeUnit.SECONDS
        );

    }

}

// why doesnt this compile ... it's supposed to (according to t'internet) - looks like its an oracle java thing, not an openjdk thing.. (i thought they were equal?)
//        int year = 1642;
//        switch (year) {
//            case 1600..1699 -> System.out.println("1600s");
//            case 1700..1799 -> System.out.println("1700s");
//            default -> System.out.println("Other");
//        }
