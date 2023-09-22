package Server;

import static spark.Spark.*;

import java.util.Arrays;

import javax.servlet.MultipartConfigElement;

import spark.Spark;
import Server.ElevenLabs.ElevenlabsEndpoint;
import Server.ElevenLabs.GetVoicesEndpoint;
import Server.HonestAI.DeleteMemoriesForCharacterEndpoint;
import Server.HonestAI.GenerateEndpoint;
import Server.HonestAI.GetCharsIDsForUserEndpoint;
import Server.HonestAI.InitMemoriesEndpoint;
import Server.HonestAI.TextGenEndpoint;
import Server.LoadBalancer.LoadBalancer;

public class Main {
    public static String endpoint = "http://184.67.78.114:40483/";

    public static void main(String[] args) {
        // Set the port for the Spark server
        port(6969);

        // Set the static file location
        staticFiles.location("/public");

        FirebaseInitializer init = new FirebaseInitializer();
        init.initialize();

        LoadBalancer loadBalancer = LoadBalancer.getInstance(Arrays.asList(
                Main.endpoint  // port 5000
        ));

        // Configure routes and endpoints
        configureRoutes(loadBalancer);

        // Start the Spark server
        init();
        Runtime.getRuntime().addShutdownHook(new Thread(Spark::stop));

    }
    // temp
    // private static final String SERVICE_ACCOUNT_KEY_PATH =
    // "C:/Users/B1llstar/Documents/Github/StarByte-WebServer/ai-anyone-firebase-adminsdk-m4zfc-d6e526a2a3.json";

    public static void configureRoutes(LoadBalancer loadBalancer) {

        // Enable CORS for all routes
        
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        });

        options("/*", (req, res) -> {
            res.status(200);
            return "OK";
        });
        

        // Example endpoint using SynthesizeSpeechWithChosenVoiceEndpoint class
        // SynthesizeSpeechWithChosenVoiceEndpoint synthesizeSpeechEndpoint = new
        // SynthesizeSpeechWithChosenVoiceEndpoint();
        // synthesizeSpeechEndpoint.handleSynthesizeSpeechRequest();
        DeleteMemoriesForCharacterEndpoint deleteMemoriesForCharacterEndpoint = new DeleteMemoriesForCharacterEndpoint(
                loadBalancer);
        deleteMemoriesForCharacterEndpoint.handleDeleteMemoriesForCharacterRequest();
        GenerateEndpoint endpoint = new GenerateEndpoint(loadBalancer);
        endpoint.handleGenerateRequest();
        GetCharsIDsForUserEndpoint getCharsIDsForUserEndpoint = new GetCharsIDsForUserEndpoint(loadBalancer);
        getCharsIDsForUserEndpoint.handleGetCharsIDsForUserEndpoint();

        ElevenlabsEndpoint elevenlabsEndpoint = new ElevenlabsEndpoint(loadBalancer);
         elevenlabsEndpoint.handleElevenlabsRequest();
        TextGenEndpoint textGen = new TextGenEndpoint(loadBalancer);
        textGen.handleTextGenRequest();
        InitMemoriesEndpoint initMemories = new InitMemoriesEndpoint(loadBalancer);
        initMemories.handleInitMemoriesRequest();
        GetVoicesEndpoint getVoices = new GetVoicesEndpoint(loadBalancer);
        // getVoices.handleGetVoicesRequest();
    }
}
