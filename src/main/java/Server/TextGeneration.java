package Server;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class TextGeneration {

    private static final String HOST = "174.170.249.197:41536";
    private static final String URI = "http://" + HOST + "/api/v1/generate";

    public static void run(String prompt) {
        try {
            URL url = new URL(URI);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            // Building the JSON request
            JSONObject request = new JSONObject();
            request.put("prompt", prompt);
            request.put("max_new_tokens", 250);
            request.put("preset", "None");
            request.put("do_sample", true);
            request.put("temperature", 0.7);
            request.put("top_p", 0.1);
            request.put("typical_p", 1);
            request.put("epsilon_cutoff", 0);
            request.put("eta_cutoff", 0);
            request.put("tfs", 1);
            request.put("top_a", 0);
            request.put("repetition_penalty", 1.18);
            request.put("top_k", 40);
            request.put("min_length", 0);
            request.put("no_repeat_ngram_size", 0);
            request.put("num_beams", 1);
            request.put("penalty_alpha", 0);
            request.put("length_penalty", 1);
            request.put("early_stopping", false);
            request.put("mirostat_mode", 0);
            request.put("mirostat_tau", 5);
            request.put("mirostat_eta", 0.1);
            request.put("seed", -1);
            request.put("add_bos_token", true);
            request.put("truncation_length", 2048);
            request.put("ban_eos_token", false);
            request.put("skip_special_tokens", true);
            request.put("stopping_strings", new JSONArray());

            OutputStream os = conn.getOutputStream();
            os.write(request.toString().getBytes());
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED &&
                    conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            StringBuilder response = new StringBuilder();
            while ((output = br.readLine()) != null) {
                response.append(output);
            }

            conn.disconnect();

            JSONObject jsonResponse = new JSONObject(response.toString());
            String result = jsonResponse.getJSONArray("results").getJSONObject(0).getString("text");

            System.out.println(prompt + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String prompt = "In order to make homemade bread, follow these steps:\n1)";
        run(prompt);
    }
}
