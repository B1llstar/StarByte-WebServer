package Server.HonestAI;

import org.json.JSONArray;
import org.json.JSONObject;

import Server.Main;
import Server.LoadBalancer.LoadBalancer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import static spark.Spark.post;

public class GenerateEndpoint {
    private LoadBalancer loadBalancer;

    public GenerateEndpoint(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    // Define your endpoint
    public void handleGenerateRequest() {
        SimpleDateFormat sdf = new SimpleDateFormat("[dd/MMM/yyyy:HH:mm:ss]");
        post("/generate", (request, response) -> {
            double temperature;
            // Parse the JSON request body
            JSONObject jsonObject = new JSONObject(request.body());
            // Check if 'temperature' field exists
if (jsonObject.has("temperature")) {
     temperature = jsonObject.getDouble("temperature");
     System.out.println("Detected temperature: " + temperature);
    // Use 'temperature' as needed
} else {
    temperature = .54;

    // Handle the case where 'temperature' is missing
}
            // Get the 'prompt' field value
            String message = jsonObject.getString("prompt");
      
            // Print the message
            System.out.println(request.ip() + " - " + sdf.format(new Date()) + " Prompt: " + message);

            StringBuilder responseBody = new StringBuilder();

            // Check if 'message' field contains a value
            if (message != null && !message.isEmpty()) {
                String usedServer = null;
           
                while (true) {
                    // Get the least busy server
                    /* 
                    String server = loadBalancer.getLeastBusyServer();
                    if (server == null) {
                        System.out.println("[CRITICAL] All servers are unreachable!");
                        response.status(500); // Internal Server Error
                        return "All servers are unreachable!";
                    }
 
*/

             usedServer = Main.endpoint + "api/v1/generate";
System.out.println("Server used: " + usedServer);
                    try {
                        // Create JSON payload for the HTTP connection
                        JSONObject payload = new JSONObject();
                        payload.put("prompt", message);
                        payload.put("max_new_tokens", 250);
                        payload.put("preset", "None");
                        payload.put("do_sample", true);
                        payload.put("temperature", temperature);
                        payload.put("top_p", 0.1);
                        payload.put("typical_p", 1);
                        payload.put("epsilon_cutoff", 0);
                        payload.put("eta_cutoff", 0);
                        payload.put("tfs", 1);
                        payload.put("top_a", 0);
                        payload.put("repetition_penalty", 1.18);
                        payload.put("top_k", 40);
                        payload.put("min_length", 0);
                        payload.put("no_repeat_ngram_size", 0);
                        payload.put("num_beams", 1);
                        payload.put("penalty_alpha", 0);
                        payload.put("length_penalty", 1);
                        payload.put("early_stopping", false);
                        payload.put("mirostat_mode", 0);
                        payload.put("mirostat_tau", 5);
                        payload.put("mirostat_eta", 0.1);
                        payload.put("seed", -1);
                        payload.put("add_bos_token", true);
                        payload.put("truncation_length", 2048);
                        payload.put("ban_eos_token", false);
                        payload.put("skip_special_tokens", true);
                        payload.put("stopping_strings", new JSONArray());
            
                      
                        URL url = new URL(usedServer);
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
                        // Get the response bo dy
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                responseBody.append(line);
                            }
                        }
                                                  System.out.println(responseBody.toString());


                        // Close the HTTP connection
                        connection.disconnect();
return responseBody.toString();
                        // Break the loop as the request was successful
                    } catch (Exception e) {
                        // Mark the server as unreachable
                        System.out.println("Server is unreachable.");
                        
                        usedServer = null;
                    }
                }

              
            } else {
                response.status(400); // Bad Request
                return "Invalid request: 'prompt' field is missing or empty.";
            }

        });
    }
}
