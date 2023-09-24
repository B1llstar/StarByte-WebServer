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
            String username = jsonObject.getString("username");
            String character = jsonObject.getString("character");
            String context = jsonObject.getString("context");
            String user_input = jsonObject.getString("user_input");
            String name1 = jsonObject.getString("name1");
            String name2 = jsonObject.getString("name2");
            JSONObject history = jsonObject.getJSONObject("history");

            // Initialize response variables
            String responseMessage = null;
            JSONObject historyObject = null;

            // Check if 'message' field contains a value
            if (user_input != null && !user_input.isEmpty()) {
                String usedServer = null;

                while (true) {
                    /* 
                    // Get the least busy server
                    String server = loadBalancer.getLeastBusyServer();
                    if (server == null) {
                        System.out.println("[CRITICAL] All servers are unreachable!");
                        break;
                    }
*/
  //                  usedServer = server;
                    usedServer = Main.endpoint + "api/v1/chat";

                    try {
                        // Create JSON payload for the HTTP connection
                        JSONObject payload = new JSONObject();
                        payload.put("character", character);
                        payload.put("name1", name1);
                        payload.put("username", username);
                        payload.put("name2", name2);
                        payload.put("context", context);
                        payload.put("user_input", user_input);

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
                        //loadBalancer.releaseServer(server);
                       // usedServer = null;
                    }
                }

                // If a server was used, reduce its load
                /* 
                if (usedServer != null) {
                    loadBalancer.releaseServer(usedServer);
                }*/
            }

            // Create a JSON response object
            JSONObject jsonResponseObject = new JSONObject();
            jsonResponseObject.put("response_message", responseMessage);
            jsonResponseObject.put("history", historyObject);

            return jsonResponseObject.toString();
        });
    }
}
// Set the port for your Spark application
