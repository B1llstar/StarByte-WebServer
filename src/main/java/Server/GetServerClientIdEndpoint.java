package Server;

import static spark.Spark.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

public class GetServerClientIdEndpoint {

    public void handleGetServerClientIDRequest() {

        System.out.println("Handling /getServerClientID request...");
        
        post("/getServerClientID", (req, res) -> {
            try {
                // Read the content of the JSON file as a string
                
                String jsonContent = new String(Files.readAllBytes(Paths.get("client_secret_19683722185-udbdro4v594t234anbdtfijq4lcceril.apps.googleusercontent.com.json")));

                // Parse the JSON string into a JSONObject
                JSONObject jsonObject = new JSONObject(jsonContent);

                // Get the "installed" object from the JSON object
                JSONObject installedObject = jsonObject.getJSONObject("installed");

                // Get the client_id from the "installed" object
                String clientId = installedObject.getString("client_id");
                

                // Print the client_id
                System.out.println("Client ID: " + clientId);
                
                // Return the client ID as the response
                return clientId;
            } catch (Exception e) {
                res.status(500);
                e.printStackTrace();
                return "Error occurred: " + e.getMessage();
            }
        });
    }
}
