package Server;

import static spark.Spark.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UploadVoiceSampleEndpoint { /* 
    public void handleUploadVoiceSample() {
    	post("/uploadVoiceSample", (req, res) -> {
            res.header("Access-Control-Allow-Origin", "*"); // Add this line
            String fname = req.queryParams("filename");
            System.out.println("filename = " + fname);
            try (InputStream fileInputStream = req.raw().getPart("file").getInputStream()) {
            	String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            	int uniqueId = 1;
            	String directory = "./audio-uploads/";
            	String filename = directory + timestamp + "_" + uniqueId + "_" + fname;

                Path filePath = Paths.get(directory, filename);
                while (Files.exists(filePath)) {
                    uniqueId += 1;
                    filename = directory + timestamp + "_" + uniqueId + "_" + fname;
                    filePath = Paths.get(directory, filename);
                }
                saveFile(fileInputStream, filename);
                return "File uploaded successfully";
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "An error occurred: " + e.getMessage();
            }
        });
    }*/
/* 
    private void saveFile(InputStream inputStream, String filename) throws IOException {
    	System.out.println("Saving file to: " + filename);
        File outputFile = new File(filename);
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }
    */
}
