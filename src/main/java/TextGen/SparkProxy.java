package TextGen;
import static spark.Spark.*;

import org.json.JSONObject;

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

public class SparkProxy {
	
	// Instantiate the LoadBalancer
    private static LoadBalancer loadBalancer = new LoadBalancer(Arrays.asList(
            "http://142.112.54.19:44309/api/v1/generate"
    		//"http://127.0.0.1:5000/api/v1/generate"
    ));

    public static void main(String[] args) {
        // Set the port for your Spark application
        port(8080);

        // Define your endpoint
        post("/textgen", (request, response) -> {
        	SimpleDateFormat sdf = new SimpleDateFormat("[dd/MMM/yyyy:HH:mm:ss]");
        	
            // Parse the JSON request body
            JSONObject jsonObject = new JSONObject(request.body());

            // Get the 'prompt' field value
            String message = jsonObject.getString("prompt");

            // Print the message
            //System.out.println(request.ip() + " - " + sdf.format(new Date()) + " Prompt: " + message);

            StringBuilder responseBody = new StringBuilder();

            // Check if 'message' field contains a value
            if (message != null && !message.isEmpty()) {
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
                        // Create JSON payload for the HTTP connection
                        JSONObject payload = new JSONObject();
                        payload.put("prompt", message);
                        //payload.put("temperature", 0.9);
                        //payload.put("repetition_penalty", 1.3);
                        //payload.put("max_new_tokens", 300);

                        URL url = new URL(server);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setDoOutput(true);

                        // Send the JSON payload
                        try (OutputStream outputStream = connection.getOutputStream()) {
                            outputStream.write(payload.toString().getBytes());
                            outputStream.flush();
                        }

                        // Get the HTTP response code
                        int responseCode = connection.getResponseCode();
                        System.out.println("HTTP response code: " + responseCode);

                        // Get the response body
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                responseBody.append(line);
                            }
                        }

                        //System.out.println(request.ip() + " - " + sdf.format(new Date()) + " Response body: " + responseBody.toString());

                        // Close the HTTP connection
                        connection.disconnect();

                        // Break the loop as the request was successful
                        break;
                    } catch (Exception e) {
                        // Mark the server as unreachable
                        loadBalancer.markAsUnreachable(server);
                        System.out.println("Server " + server + " is unreachable. Moving on to next server.");
                        loadBalancer.releaseServer(server);
                        usedServer = null;
                    }
                }
                
                // If a server was used, reduce its load
                if (usedServer != null) {
                    loadBalancer.releaseServer(usedServer);
                }
            }
            
            // Return a response
            return responseBody;
        });


        // Stop the Spark application gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(Spark::stop));
    }
}
