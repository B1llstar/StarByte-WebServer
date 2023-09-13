package Core;

import static spark.Spark.*;

import org.json.JSONArray;
import org.json.JSONObject;

import TextGen.LoadBalancer;
import spark.Spark;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

   class InitMemoriesEndpoint {
    
        private LoadBalancer loadBalancer;
        private static final AtomicInteger charIdCounter = new AtomicInteger(0);
        InitMemoriesEndpoint(LoadBalancer loadBalancer) {
            this.loadBalancer = loadBalancer;
            
        }
        public void initMemoriesRequest() {
            port(6969);
            post("/initMemories", (request, response) -> {
                SimpleDateFormat sdf = new SimpleDateFormat("[dd/MMM/yyyy:HH:mm:ss]");

                // Parse the JSON request body
                JSONObject jsonObject = new JSONObject(request.body());

                // Get the 'char_name' and 'username' fields from the JSON
                String charName = jsonObject.getString("char_name");
                String username = jsonObject.getString("username");

                // Create a unique char_id for each request
                String charId = Integer.toString(charIdCounter.incrementAndGet());

                // Combine 'char_name' and 'username' to create a JSON payload
                JSONObject payload = new JSONObject();
                payload.put("char_name", charName);
                payload.put("username", username);

                StringBuilder responseBody = new StringBuilder();

                // Check if 'char_name' and 'username' are not empty
                if (!charName.isEmpty() && !username.isEmpty()) {
                    String usedServer = null;

                    while (true) {
                        // Get the least busy server
                        String server = loadBalancer.getLeastBusyServer();
                        if (server == null) {
                            System.out.println("[CRITICAL] All servers are unreachable!");
                            break;
                        }

                        usedServer = server;

                        try {
                            // Send the JSON payload to the server
                            URL url = new URL(server);
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
                            try (BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(connection.getInputStream()))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    responseBody.append(line);
                                }
                            }

                            // Close the HTTP connection
                            connection.disconnect();

                            // Break the loop as the request was successful
                            break;
                        } catch (Exception e) {
                            // Mark the server as unreachable
                            loadBalancer.markAsUnreachable(server);
                            System.out.println("Server " + server + " is unreachable. Moving on to the next server.");
                            loadBalancer.releaseServer(server);
                            usedServer = null;
                        }
                    }

                    // If a server was used, reduce its load
                    if (usedServer != null) {
                        loadBalancer.releaseServer(usedServer);
                    }
                }

                // Return a response with the generated char_id
                JSONObject responseJson = new JSONObject();
                responseJson.put("char_id", charId);
                responseJson.put("message", "File created successfully");
                response.status(201);

                return responseJson.toString();

            });
        }
    }