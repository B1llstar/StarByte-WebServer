

import static spark.Spark.*;

public class Main {

    public static void main(String[] args) {
        // Set the port for the Spark server
        port(8080);

        // Set the static file location
        staticFiles.location("/public");

        // Configure routes and endpoints
        configureRoutes();

        // Start the Spark server
        init();
    }

    public static void configureRoutes() {
        // Register your endpoints here
        //get("/hello", (req, res) -> "Hello, world!");

        // Example endpoint using SynthesizeSpeechWithChosenVoiceEndpoint class
        SynthesizeSpeechWithChosenVoiceEndpoint synthesizeSpeechEndpoint = new SynthesizeSpeechWithChosenVoiceEndpoint();
        synthesizeSpeechEndpoint.handleSynthesizeSpeechRequest();

        // Add more routes here
        // For example:
        // post("/another-route", (req, res) -> "Response for /another-route");
        // get("/hello", (req, res) -> "Hello, world!");
    }

}
