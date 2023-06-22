package Server;


import static spark.Spark.*;

import javax.servlet.MultipartConfigElement;

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
           // Enable CORS for all routes
    	before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            response.header("Access-Control-Allow-Credentials", "true");
            request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp"));
        });
        // Example endpoint using SynthesizeSpeechWithChosenVoiceEndpoint class
        SynthesizeSpeechWithChosenVoiceEndpoint synthesizeSpeechEndpoint = new SynthesizeSpeechWithChosenVoiceEndpoint();
        synthesizeSpeechEndpoint.handleSynthesizeSpeechRequest();

        UploadVoiceSampleEndpoint uploadSpeechFromSampleEndpoint = new UploadVoiceSampleEndpoint();
        uploadSpeechFromSampleEndpoint.handleUploadVoiceSample();

        // Add more routes here
        // For example:
        // post("/another-route", (req, res) -> "Response for /another-route");
        // get("/hello", (req, res) -> "Hello, world!");
    }

}
