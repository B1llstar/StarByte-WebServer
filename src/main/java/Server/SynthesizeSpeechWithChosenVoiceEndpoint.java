package Server;


import static spark.Spark.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.json.JSONTokener;

public class SynthesizeSpeechWithChosenVoiceEndpoint {
    public void handleSynthesizeSpeechRequest() {
        post("/tts", (req, res) -> {
            JSONObject payload = new JSONObject(new JSONTokener(req.body()));

            String text = payload.getString("text");
            String voice_id = payload.getString("voice_id"); // Replace with the voice ID you want to use
            System.out.println("text: " + text);
            System.out.println("voice_id: " + voice_id);
            if (text != null && !text.isEmpty()) {
                try {
                    byte[] audioData = generateSpeech(text, voice_id);

                    res.header("Content-Disposition", "attachment; filename=output.mp3");
                    res.type("audio/mpeg");
                    
                    res.raw().getOutputStream().write(audioData);
                    res.raw().getOutputStream().flush();

                    return res.raw();
                } catch (Exception e) {
                    e.printStackTrace();
                    res.status(500);
                    return "An error occurred: " + e.getMessage();
                }
            } else {
                res.status(400);
                return "Missing or empty 'text' parameter";
            }
        });
    }

    private byte[] generateSpeech(String text, String voice_id) throws IOException {
        String urlStr = "https://api.elevenlabs.io/v1/text-to-speech/" + voice_id;
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        String xi_api_key = System.getenv("ELEVENLABS");
        System.out.println("APIKEY: " + xi_api_key);

        // Set request headers
        connection.setRequestProperty("Content-Type", "application/json");
       // connection.setRequestProperty("xi-api-key", xi_api_key);
               connection.setRequestProperty("xi-api-key", xi_api_key);

        connection.setRequestProperty("accept", "audio/mpeg");
String payload = "{ \"text\": \"" + text + "\", \"model_id\": \"eleven_monolingual_v1\"}";

        // Set request payload
        connection.getOutputStream().write(payload.getBytes("UTF-8"));

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream())) {
                return in.readAllBytes();
            }
        } else {
            throw new IOException("Error response: " + connection.getResponseMessage());
        }
    }
}
