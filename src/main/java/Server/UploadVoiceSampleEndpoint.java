package Server;

import static spark.Spark.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UploadVoiceSampleEndpoint {
    public void handleUploadVoiceSample() {
        options("/uploadVoiceSample", (req, res) -> {
            String accessControlRequestHeaders = req.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                res.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = req.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                res.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((req, res) -> res.header("Access-Control-Allow-Origin", "*"));

        post("/uploadVoiceSample", (req, res) -> {
            res.header("Access-Control-Allow-Origin", "*"); // Add this line
            try (InputStream fileInputStream = req.raw().getPart("file").getInputStream()) {
                String filename = "audio-uploads/" + req.queryParams("filename"); // Use the provided filename parameter
                saveFile(fileInputStream, filename);
                return "File uploaded successfully";
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "An error occurred: " + e.getMessage();
            }
        });
    }

    private void saveFile(InputStream inputStream, String filename) throws IOException {
        File outputFile = new File(filename);
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}
