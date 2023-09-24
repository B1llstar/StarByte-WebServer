package Server.ElevenLabs;

import static spark.Spark.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import Server.LoadBalancer.LoadBalancer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class ElevenlabsEndpoint {

    private LoadBalancer loadBalancer;

    public ElevenlabsEndpoint(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;

    }

    private static final String ELEVENLABS_API_URL = "https://api.elevenlabs.io/v1/text-to-speech/";

    private static final String GCS_BUCKET_NAME = "ai-anyone.appspot.com";
    private static final String TEMP_AUDIO_COLLECTION = "temp_audio/";

    private static final String SERVICE_ACCOUNT_KEY_PATH = "C:/Users/B1llstar/Documents/Github/StarByte-WebServer/ai-anyone-firebase-adminsdk-m4zfc-d6e526a2a3.json";

    // Define a route for /tts

    public void handleElevenlabsRequest() {

        post("/tts", (req, res) -> {
            // port(6969); // Replac

            JSONObject payload = new JSONObject(new JSONTokener(req.body()));

            String userId = payload.getString("uid");
            String text = payload.getString("text");
            String voiceId = payload.getString("voice_id"); //
            String xiApiKey = payload.getString("xi-api-key");

            if (userId != null && !userId.isEmpty() && text != null && !text.isEmpty() && voiceId != null
                    && !voiceId.isEmpty()) {
                // Call the generateSpeech method to obtain audio data
                byte[] audioData = generateSpeech(text, voiceId, xiApiKey);

                if (audioData != null && audioData.length > 0) {
                    // Step 4: Server writes the audio response to a temporary file locally
                    String tempFileName = writeAudioDataToLocalFile(audioData);

                    if (tempFileName != null) {
                        // Step 5: Server uploads the temporary audio file to Google Cloud Storage
                        String uploadedFileUrl = uploadFileToGCS(tempFileName, userId);

                        if (uploadedFileUrl != null) {
                            // Step 6: If the upload succeeds, server obtains the URL of the uploaded file

                            // Step 7: Server deletes the local temporary audio file
                            deleteLocalFile(tempFileName);

                            // Step 8: Server includes the URL of the uploaded audio file in the response
                            res.status(200);
                            res.header("Content-Type", "application/json");
                            return "{\"audioUrl\":\"" + uploadedFileUrl + "\"}"; // Return the URL in JSON format
                        } else {
                            res.status(500); // Internal Server Error
                            return "Upload to Google Cloud Storage failed.";
                        }
                    } else {
                        res.status(500); // Internal Server Error
                        return "Failed to write audio data to a local file.";
                    }
                } else {
                    res.status(400); // Bad Request
                    return "Audio data from generateSpeech is empty or invalid.";
                }
            } else {
                res.status(400); // Bad Request
                return "Missing or invalid parameters: userId, text, voiceId.";
            }
        });
    }

    private String getFirebaseStorageFileURL(String filePath) {
        try {
            // Initialize Firebase App
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(SERVICE_ACCOUNT_KEY_PATH)))
                    .setStorageBucket(GCS_BUCKET_NAME) // Set the bucket name
                    .build();
            FirebaseApp.initializeApp(options);

            // Initialize Firebase Storage
            Storage storage = StorageOptions.getDefaultInstance().getService();

            // Get the reference to the file
            BlobId blobId = BlobId.of(GCS_BUCKET_NAME, filePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

            // Get the download URL for the file
            Blob blob = storage.get(blobId);

            if (blob != null) {
                return blob.getMediaLink();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] generateSpeech(String text, String voiceId, String xiApiKey) throws IOException {
        String urlStr = ELEVENLABS_API_URL + voiceId;
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // Set request headers
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("xi-api-key", xiApiKey);

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

    private String writeAudioDataToLocalFile(byte[] audioData) throws IOException {
        String projectBaseDir = System.getProperty("user.dir");
        String tempFileName = projectBaseDir + "/temp_audio/output.mp3";
        try (FileOutputStream outputStream = new FileOutputStream(tempFileName)) {
            outputStream.write(audioData);
        }
        return tempFileName;
    }

    private String uploadFileToGCS(String filePath, String userId) throws IOException {
        StorageOptions storageOptions = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(new FileInputStream(SERVICE_ACCOUNT_KEY_PATH)))
                .build();

        Storage storage = storageOptions.getService();

        BlobId blobId = BlobId.of(GCS_BUCKET_NAME,
                TEMP_AUDIO_COLLECTION + userId + "/" + UUID.randomUUID().toString() + ".mp3");
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        // Upload the file to Google Cloud Storage
        storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

        // Retrieve and return the URL of the uploaded file
        Blob blob = storage.get(blobId);

        if (blob != null) {
            System.out.println("Got a url!" + blob.getMediaLink());
            return blob.getMediaLink();
        } else {
            return null;
        }
    }

    private void deleteLocalFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
}
// insert tts endpoint here
