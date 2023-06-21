package Server;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileDownloadClient {

    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080/tts"; // Replace with your server URL
        String outputFile = "output.mp3"; // Path to save the downloaded file
        String voiceId = "RMPmUYE4vUnCJBQysq5L"; // Replace with desired voice ID
        String apiKey = System.getenv("ELEVENLABS"); // Replace with your XI API key
        String text = "No two things are more alike, than Bacon and pelicans."; // Replace with desired text

        try {
            downloadFile(serverUrl, outputFile, voiceId, text);
        } catch (IOException e) {
            System.err.println("Error downloading file: " + e.getMessage());
        }
    }

    public static void downloadFile(String serverUrl, String outputFile, String voiceId, String text) throws IOException {
        URL url = new URL(serverUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // Set request headers
            
        connection.setRequestProperty("Content-Type", "application/json");

        // Set request payload
        String payload = "{ \"text\": \"" + text + "\", \"voice_id\": \"" + voiceId + "\" }";
        connection.getOutputStream().write(payload.getBytes("UTF-8"));
        
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } else {
            System.out.println("Error response: " + connection.getResponseMessage());
        }

        connection.disconnect();
    }
}
