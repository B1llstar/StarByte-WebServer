package Server;

import static spark.Spark.*;

import org.json.JSONObject;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import net.andrewcpu.elevenlabs.ElevenLabsAPI;
import net.andrewcpu.elevenlabs.elements.VoiceBuilder;
import net.andrewcpu.elevenlabs.elements.voice.Voice;
import net.andrewcpu.elevenlabs.exceptions.ElevenLabsException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// 1. Upload the files to the DB for Submission
// 2. Grab File Urls
// 3. Make the voices using file URLS
// 4. 200? Delete the files
// 5. Voice has been created
public class MakeNewVoiceFromSamplesEndpoint {
    private static ElevenLabsAPI client;

    public String handleMakeNewVoiceFromSamplesRequest(String aiId, String userId) {
        client = ElevenLabsAPI.getInstance();
        client.setAPIKey(System.getenv("ELEVENLABS"));
        String[] fileUrls = getFileUrlsFromDatabase(userId);
        String[] fileNames = downloadFilesFromStorage(fileUrls);
        String voiceId = makeVoice(aiId, fileNames, "accent", "japanese");
                deleteLocalFiles(fileNames);
        JSONObject response = new JSONObject();
        response.put("message", "Voice created successfully!");
        response.put("voice_id", voiceId);

        deleteLocalFiles(fileNames);
        return response.toString();
    }

    private static String[] getFileUrlsFromDatabase(String id) {
        List<String> fileUrls = new ArrayList<>();

        // Create a Firestore client
        FirestoreOptions firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
                .setProjectId("ai-anyone")
                .build();
        Firestore firestore = firestoreOptions.getService();

        try {
            // Retrieve the file URLs from Firestore
            ApiFuture<DocumentSnapshot> future = firestore.collection("temp_files")
                    .document(id)
                    .get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                // Assuming the file URLs are stored in an array field called "audio_samples"
                Object audioSamples = document.get("audio_samples");
                if (audioSamples instanceof List<?>) {
                    List<?> urls = (List<?>) audioSamples;
                    for (Object url : urls) {
                        if (url instanceof String) {
                            fileUrls.add((String) url);
                        }
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Convert the list of URLs to an array
        String[] fileUrlsArray = fileUrls.toArray(new String[0]);

        return fileUrlsArray;
    }
    private static String extractFileNameFromUrl(String fileUrl) {
        int slashIndex = fileUrl.lastIndexOf('/');
        int questionMarkIndex = fileUrl.lastIndexOf('?');
        if (slashIndex != -1 && questionMarkIndex != -1 && slashIndex < questionMarkIndex) {
            return fileUrl.substring(slashIndex + 1, questionMarkIndex);
        } else if (slashIndex != -1) {
            return fileUrl.substring(slashIndex + 1);
        }
        return null;
    }
    private static String downloadFile(String fileUrl, String fileName) {
    try {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = connection.getInputStream();
            String filePath = "audio/" + fileName;  // Adjust the file path as per your requirement
            FileOutputStream outputStream = new FileOutputStream(filePath);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            return filePath;
        } else {
            System.out.println("Failed to download file: " + fileName + ". Response code: " + responseCode);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    return null;
}

    private static String[] downloadFilesFromStorage(String[] fileUrls) {
        List<String> fileNames = new ArrayList<>();
    
        for (String fileUrl : fileUrls) {
            // Extract the file name from the URL
            String fileName = extractFileNameFromUrl(fileUrl);
            
            // Download the file from the URL and store it locally
            String filePath = downloadFile(fileUrl, fileName);
            
            // Add the downloaded file path to the list of file names
            fileNames.add(filePath);
        }
    
        return fileNames.toArray(new String[0]);
    }

    private static void deleteLocalFiles(String[] filePaths) {
        for (String filePath : filePaths) {
            File fileToDelete = new File(filePath);
            if (fileToDelete.exists() && fileToDelete.isFile()) {
                if (fileToDelete.delete()) {
                    System.out.println("Deleted local file: " + filePath);
                } else {
                    System.err.println("Failed to delete local file: " + filePath);
                }
            }
        }
    }
    

    private static String makeVoice(String voiceName, String[] filePaths, String labelName, String labelValue) {
        try {
            VoiceBuilder builder = new VoiceBuilder();
            
            builder.withName(voiceName);
            for (String filePath : filePaths) {
                builder.withFile(new File(filePath));
            }
            builder.withLabel(labelName, labelValue);
            Voice voice = builder.create();
            
            return voice.getVoiceId();
        } catch (ElevenLabsException e) {
            e.printStackTrace();
        }
        return null;
    }
}
