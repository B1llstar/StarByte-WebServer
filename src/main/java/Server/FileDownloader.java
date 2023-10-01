package Server;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public class FileDownloader {

    private static final String SERVICE_ACCOUNT_KEY_PATH = "/etc/opt/secret/fb_key/ai-anyone-firebase-adminsdk-m4zfc-d6e526a2a3.json";
    private static final String BUCKET_NAME = "ai-anyone";
    private static final String DESTINATION_FOLDER = "audio/";

    public static void downloadFile(String bucketName, String objectName, String destinationFilePath) throws IOException {
        StorageOptions storageOptions = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(FileDownloader.class.getResourceAsStream(SERVICE_ACCOUNT_KEY_PATH)))
                .build();

        Storage storage = storageOptions.getService();
        Blob blob = storage.get(BlobId.of(bucketName, objectName));
        if (blob != null) {
            try (ReadableByteChannel channel = blob.reader();
                 FileOutputStream outputStream = new FileOutputStream(destinationFilePath)) {
                outputStream.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
            }
            System.out.println("File downloaded successfully: " + destinationFilePath);
        } else {
            System.out.println("File not found: " + objectName);
        }
    }

    public static void main(String[] args) throws IOException {
        String userId = "HFHed5lrrMVKgQJLdwL0Gcz5sua2";
        String aiId = "144719";
        String sampleFileName = "sample_1.wav";

        String objectName = String.format("users/%s/library/%s/audio_samples/%s", userId, aiId, sampleFileName);
        String destinationFilePath = DESTINATION_FOLDER + "/" + sampleFileName;

        downloadFile(BUCKET_NAME, objectName, destinationFilePath);
    }
}
