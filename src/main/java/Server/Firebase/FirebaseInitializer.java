package Server.Firebase; 
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.FileInputStream;

public class FirebaseInitializer {
    // Initialize Firebase services
    private static final String SERVICE_ACCOUNT_KEY_PATH = "/etc/opt/secret/fb_key/ai-anyone-firebase-adminsdk-m4zfc-d6e526a2a3.json";
    public static void initialize() {
        try {
            FileInputStream serviceAccount = new FileInputStream(SERVICE_ACCOUNT_KEY_PATH);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get a reference to Firestore
    public static Firestore getFirestore() {
        // Get the FirebaseApp instance
        FirebaseApp app = FirebaseApp.getInstance();

        // Get a Firestore instance from FirebaseApp
        return FirestoreClient.getFirestore(app);
    }
}
