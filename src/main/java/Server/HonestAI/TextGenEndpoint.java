package Server.HonestAI;

import static spark.Spark.*;

import org.json.JSONArray;
import org.json.JSONObject;

import Server.Main;
import Server.LoadBalancer.LoadBalancer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import static spark.Spark.post;

public class TextGenEndpoint {
    private LoadBalancer loadBalancer;

    public TextGenEndpoint(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public void handleTextGenRequest() {
        // port(6969); don't think it's needed since we're using
        // main in a different file now
        // Define your endpoint
        post("/textgen", (request, response) -> {

            // Parse the JSON request body
            JSONObject jsonObject = new JSONObject(request.body());
            double temperature;
            String username = jsonObject.getString("username");
            String character = jsonObject.getString("character");
            String context = jsonObject.getString("context");
            String user_input = jsonObject.getString("user_input");
            String name1 = jsonObject.getString("name1");
            String name2 = jsonObject.getString("name2");
            JSONObject history = jsonObject.getJSONObject("history");
            if (jsonObject.has("temperature")) {
                temperature = jsonObject.getDouble("temperature");
               // Use 'temperature' as needed
           } else {
               temperature = .54;
           
               // Handle the case where 'temperature' is missing
           }
 
            // Initialize response variables
            String responseMessage = null;
            JSONObject historyObject = null;

            // Check if 'message' field contains a value
            if (user_input != null && !user_input.isEmpty()) {
                String usedServer = null;

                while (true) {
                    /*
                     * // Get the least busy server
                     * String server = loadBalancer.getLeastBusyServer();
                     * if (server == null) {
                     * System.out.println("[CRITICAL] All servers are unreachable!");
                     * break;
                     * }
                     */
                    // usedServer = server;
                    usedServer = Main.endpoint + "api/v1/chat";
                  
                    try {
                        history = sanitizeHistory(history);
                        // Create JSON payload for the HTTP connection
                        JSONObject payload = new JSONObject();
                        payload.put("character", character);
                        payload.put("name1", name1);
                        payload.put("username", username);
                        payload.put("name2", name2);
                        payload.put("context", context);
                        payload.put("user_input", user_input);
                        payload.put("history", history);
                        payload.put("temperature", temperature);
                      
                        // Send the JSON payload
                        URL url = new URL(usedServer);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setDoOutput(true);

                        try (OutputStream outputStream = connection.getOutputStream()) {
                            outputStream.write(payload.toString().getBytes());
                            outputStream.flush();
                        }

                        // Get the HTTP response code
                        int responseCode = connection.getResponseCode();
                        System.out.println("HTTP response code: " + responseCode);

                        // Get the response body
                        StringBuilder responseBody = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                responseBody.append(line);
                            }
                        }

                        // Parse the JSON response from the server
                        JSONObject jsonResponse = new JSONObject(responseBody.toString());

                        // Extract the second element of the last internal array
                        JSONArray resultsArray = jsonResponse.getJSONArray("results");
                        if (resultsArray.length() > 0) {
                            JSONObject firstResult = resultsArray.getJSONObject(0);
                            if (firstResult.has("history")) {
                                JSONObject history2 = firstResult.getJSONObject("history");
                                if (history.has("internal")) {
                                    JSONArray internalArray = history2.getJSONArray("internal");
                                    if (internalArray.length() > 0) {
                                        JSONArray lastInternalArray = internalArray
                                                .getJSONArray(internalArray.length() - 1);
                                        if (lastInternalArray.length() >= 2) {
                                            responseMessage = lastInternalArray.getString(1);
                                        }
                                    }
                                }
                                historyObject = firstResult.getJSONObject("history");

                            }
                        }

                        // Close the HTTP connection
                        connection.disconnect();
                        // Create a JSON response object
                        JSONObject jsonResponseObject = new JSONObject();
                        jsonResponseObject.put("response_message", responseMessage);
                        jsonResponseObject.put("history", historyObject);

                        return jsonResponseObject.toString();
                        // Break the loop as the request was successful
                    } catch (Exception e) {
                        // Mark the server as unreachable
                        // loadBalancer.markAsUnreachable(server);
                        System.out.println("Server " + usedServer + " is unreachable.");
                        break;
                        // loadBalancer.releaseServer(server);
                        // usedServer = null;
                    }
                }

                // If a server was used, reduce its load
                /*
                 * if (usedServer != null) {
                 * loadBalancer.releaseServer(usedServer);
                 * }
                 */
            }

            // Create a JSON response object
            JSONObject jsonResponseObject = new JSONObject();
            jsonResponseObject.put("response_message", responseMessage);
            jsonResponseObject.put("history", historyObject);

            return jsonResponseObject.toString();
        });
    }

    // Helper method to sanitize the "history" object
    private JSONObject sanitizeHistory(JSONObject history) {
        JSONArray internalArray = history.getJSONArray("internal");
        JSONArray visibleArray = history.getJSONArray("visible");

        JSONArray uniqueInternal = new JSONArray();
        JSONArray uniqueVisible = new JSONArray();

        for (int i = 0; i < internalArray.length(); i++) {
            JSONArray currentArray = internalArray.getJSONArray(i);
            if (!containsDuplicate(uniqueInternal, currentArray)) {
                uniqueInternal.put(currentArray);
            }
        }

        for (int i = 0; i < visibleArray.length(); i++) {
            JSONArray currentArray = visibleArray.getJSONArray(i);
            if (!containsDuplicate(uniqueVisible, currentArray)) {
                uniqueVisible.put(currentArray);
            }
        }

        JSONObject sanitizedHistory = new JSONObject();
        sanitizedHistory.put("internal", uniqueInternal);
        sanitizedHistory.put("visible", uniqueVisible);

        return sanitizedHistory;
    }

    // Helper method to check if an array already exists in the list
    boolean containsDuplicate(JSONArray arrayToCheck, JSONArray currentArray) {
        for (int i = 0; i < arrayToCheck.length(); i++) {
            JSONArray existingArray = arrayToCheck.getJSONArray(i);
            if (areArraysEqual(existingArray, currentArray)) {
                return true;
            }
        }
        return false;
    }

    // Helper method to check if two JSON arrays are equal
    boolean areArraysEqual(JSONArray array1, JSONArray array2) {
        if (array1.length() != array2.length()) {
            return false;
        }
        for (int i = 0; i < array1.length(); i++) {
            if (!array1.get(i).equals(array2.get(i))) {
                return false;
            }
        }
        return true;
    }

}
// Set the port for your Spark application
