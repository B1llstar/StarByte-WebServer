package Server;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class StreamChat {
    private static final String HOST = "174.170.249.197:41569";
    private static final String WS_URI = "ws://" + HOST + "/api/v1/stream";
    private static final int PORT = 8080; // The port on which the server will listen
    private static String prompt = "";

    public static void main(String[] args) {
        try {
            WebSocketServer server = new WebSocketServer(new InetSocketAddress(PORT)) {
                private WebSocket conn;
                private WebSocketClient client;

                @Override
                public void onOpen(WebSocket conn, ClientHandshake handshake) {
                    System.out.println("Opened connection with client");
                    this.conn = conn;

                    try {
                        client = new WebSocketClient(new URI(WS_URI)) {
                            @Override
                            public void onOpen(ServerHandshake handshakedata) {
                                System.out.println("Connected to remote server");

                                /*String prompt = "Mizuki's Persona: You are a cute anime girl. " +
                                        "Entertain the person you are chatting with.\n<START>\nYou: " +
                                        "Tell me about yourself?\nMizuki:";*/

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

                                send(request.toString());
                            }

                            @Override
                            public void onMessage(String message) {
                                System.out.println("Received message from remote server");
                                if (conn != null) {
                                    conn.send(message); // Forward the message to the client
                                }
                            }

                            @Override
                            public void onClose(int code, String reason, boolean remote) {
                                System.out.println("Connection to remote server closed");
                            }

                            @Override
                            public void onError(Exception ex) {
                                ex.printStackTrace();
                            }
                        };
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    if (client != null) {
                        client.connect();
                    }
                }

                @Override
                public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                    System.out.println("Closed connection with client");
                    if (client != null) {
                        client.close(); // Close the connection to the remote server when the client disconnects
                    }
                }

                @Override
                public void onMessage(WebSocket conn, String message) {
                    System.out.println("Received message from client");
                    JSONObject jsonObject = new JSONObject(message);
                    if (jsonObject.has("message")) {
                    	prompt = jsonObject.getString("message");
                        System.out.println("Received prompt from client: " + prompt);
                    }

                    try {
                        if (client != null) {
                            client.send(message); // Forward the message to the remote server
                        }
                    } catch (WebsocketNotConnectedException e) {
                        //System.out.println("Exception occurred when sending message to remote server: " + e.getMessage());
                        // Handle the exception appropriately (e.g., log it, retry, or take other actions)
                    }
                }

                @Override
                public void onError(WebSocket conn, Exception ex) {
                    ex.printStackTrace();
                }

                @Override
                public void onStart() {
                    System.out.println("Server started");
                }
            };

            server.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
