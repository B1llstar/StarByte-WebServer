package Server.ElevenLabs;

import static spark.Spark.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import Server.Firebase.FirebaseInitializer;
import Server.LoadBalancer.LoadBalancer;

public class GetVoicesEndpoint {

    private LoadBalancer loadBalancer;

    public GetVoicesEndpoint(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public void handleGetVoicesRequest() {
      
                    System.out.println("Handling /getVoices request...");

        // Your other route configurations...

        post("/getVoices", (req, res) -> {

            String elevenLabsEndpoint = "https://api.elevenlabs.io/v1/voices";
            // Parse the JSON request body
            JSONObject requestBody = new JSONObject(req.body());

              String xiApiKey = requestBody.getString("xi-api-key");
  
            // ---
            // Get the user id
            // Check the user id in firestore
            // find relevant directory
            // ---
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(elevenLabsEndpoint))
                    .header("xi-api-key", xiApiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                    HttpResponse.BodyHandlers.ofString());

            // Parse the JSON response into a JSONObject
            JSONObject responseJson = new JSONObject(response.body());
            try {
                JSONArray voicesArray = responseJson.getJSONArray("voices");

                if (response.statusCode() == 200) {
                    // Create a list to store VoiceData objects
                    List<VoiceData> voiceDataList = new ArrayList<>();

                    // Iterate over the JSON array and extract voice_id and name
                    for (int i = 0; i < voicesArray.length(); i++) {
                        JSONObject voiceObject = voicesArray.getJSONObject(i);
                        String voiceId = voiceObject.getString("voice_id");
                        String name = voiceObject.getString("name");
                        System.out.println("Voice ID: " + voiceId);

                        // Create a VoiceData object and populate it with voice_id and name
                        VoiceData voiceData = new VoiceData();
                        voiceData.setVoiceId(voiceId);
                        voiceData.setName(name);

                        // Add the VoiceData object to the list
                        voiceDataList.add(voiceData);
                    }

                    // At this point, voiceDataList contains VoiceData objects with voice_id and
                    // name pairs
                    // You can work with this list as needed
                    // For example, you can store it in a database or return it as a JSON response

                    // Return the JSON array as a response
                    // Create a JSON array to store the VoiceData objects
                    JSONArray voiceDataArray = new JSONArray();
                    for (VoiceData voiceData : voiceDataList) {
                        JSONObject voiceDataObject = new JSONObject();
                        voiceDataObject.put("voice_id", voiceData.getVoiceId());
                        voiceDataObject.put("name", voiceData.getName());
                        voiceDataArray.put(voiceDataObject);
                    }
                    res.status(200);
                    res.type("application/json"); // Set the response content type to JSON
                    return voiceDataArray.toString();
                } else {
                    res.status(500);
                    return "Failed to fetch voices from Eleven Labs.";
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "Failed to parse JSON response.";
            }
        });
    }
}
