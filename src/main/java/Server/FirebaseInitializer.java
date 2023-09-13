package Server;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;

public class FirebaseInitializer {
    // key for debugging
    private static final String SERVICE_ACCOUNT_KEY_PATH = "C:/Users/B1llstar/Documents/Github/StarByte-WebServer/ai-anyone-firebase-adminsdk-m4zfc-d6e526a2a3.json";
    public static void initialize() {
        try {
            
        //    FileInputStream serviceAccount = new FileInputStream("/etc/opt/secret/fb_key");
        
            FileInputStream serviceAccount = new FileInputStream(SERVICE_ACCOUNT_KEY_PATH);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
