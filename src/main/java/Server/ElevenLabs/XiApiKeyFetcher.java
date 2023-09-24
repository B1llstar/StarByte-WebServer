package Server.ElevenLabs;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class XiApiKeyFetcher {
    private final Firestore firestore;
    private final Map<String, String> cache; // Local cache to store xi-api-key for users

    public XiApiKeyFetcher(Firestore firestore) {
        this.firestore = firestore;
        this.cache = new HashMap<>(); // Initialize the cache
    }

    public String fetchAPIKey(String userId) {
        // Check the cache first
        if (cache.containsKey(userId)) {
            return cache.get(userId);
        } else {
            // Not found in cache, fetch from Firestore
            CollectionReference usersCollection = firestore.collection("users");
            DocumentReference userDocument = usersCollection.document(userId);
            ApiFuture<DocumentSnapshot> apiFuture = userDocument.get();

            try {
                DocumentSnapshot document = apiFuture.get();
                if (document.exists()) {
                    Map<String, Object> userData = document.getData();
                    if (userData != null && userData.containsKey("elevenlabs")) {
                        Map<String, Object> userProperties = (Map<String, Object>) userData.get("elevenlabs");
                        if (userProperties != null && userProperties.containsKey("xi-api-key")) {
                            String xiApiKey = (String) userProperties.get("xi-api-key");
                            if (xiApiKey != null) {
                                // Store in cache
                                cache.put(userId, xiApiKey);
                                return xiApiKey;
                            }
                        }
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return null; // Return null if no xi-api-key is found
    }
}
