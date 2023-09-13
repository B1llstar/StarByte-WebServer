package Server;

import static spark.Spark.*;

import java.util.Arrays;

import javax.servlet.MultipartConfigElement;

import Server.ElevenlabsEndpoint;
import TextGen.LoadBalancer;
import TextGen.TextGenEndpoint;
import spark.Spark;
import Core.InitMemoriesEndpoint;

public class Main {

    public static void main(String[] args) {
        // Set the port for the Spark server
        port(6969);

        // Set the static file location
        staticFiles.location("/public");
        LoadBalancer loadBalancer = LoadBalancer.getInstance(Arrays.asList(
                "http://184.67.78.114:41823/api/v1/chat" // port 5000
        ));

        // Configure routes and endpoints
        configureRoutes(loadBalancer);

        // Start the Spark server
        init();
        Runtime.getRuntime().addShutdownHook(new Thread(Spark::stop));

    }

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
        SynthesizeSpeechWithChosenVoiceEndpoint synthesizeSpeechEndpoint = new SynthesizeSpeechWithChosenVoiceEndpoint();
        synthesizeSpeechEndpoint.handleSynthesizeSpeechRequest();

        ElevenlabsEndpoint elevenlabsEndpoint = new ElevenlabsEndpoint(loadBalancer);
        elevenlabsEndpoint.handleElevenlabsRequest();
        TextGenEndpoint textGen = new TextGenEndpoint(loadBalancer);
        textGen.handleTextGenRequest();
        InitMemoriesEndpoint initMemories = new InitMemoriesEndpoint(loadBalancer);
        initMemories.handleInitMemoriesRequest();
    }
}
