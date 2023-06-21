package Server;

import static spark.Spark.*;


import java.util.List;
import org.json.JSONObject;
import org.json.JSONTokener;

import net.andrewcpu.elevenlabs.ElevenLabsAPI;
import net.andrewcpu.elevenlabs.api.*;
import net.andrewcpu.elevenlabs.api.net.ElevenLabsRequest;
import net.andrewcpu.elevenlabs.elements.VoiceBuilder;
import net.andrewcpu.elevenlabs.elements.voice.Voice;
import net.andrewcpu.elevenlabs.elements.voice.VoiceSettings;
import net.andrewcpu.elevenlabs.enums.HTTPMethod;
import net.andrewcpu.elevenlabs.exceptions.ElevenLabsException;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;

public class MakeNewVoiceFromSamplesEndpoint {
/*    private static List<Voice> voices;
    private static Voice voice;
    private static ElevenLabsAPI client;

        public void handleMakeNewVoiceFromSamplesRequest() {
             post("/makeNewVoiceFromSamples", (req, res) -> {
            JSONObject payload = new JSONObject(new JSONTokener(req.body()));
            
            String text = payload.getString("text");
            String voice_id = payload.getString("voice_id"); // Replace with the voice ID you want to use
           ;
            if (text != null && !text.isEmpty()) {
                try {
                    byte[] audioData = generateSpeech(text, voice_id);

                    res.header("Content-Disposition", "attachment; filename=output.mp3");
                    res.type("audio/mpeg");
                    
                    res.raw().getOutputStream().write(audioData);
                    res.raw().getOutputStream().flush();

                    return res.raw();
                } catch (Exception e) {
                    e.printStackTrace();
                    res.status(500);
                    return "An error occurred: " + e.getMessage();
                }
            } else {
                res.status(400);
                return "Missing or empty 'text' parameter";
            }
        });
            client = ElevenLabsAPI.getInstance(); // create client instance
        client.setAPIKey(System.getenv("ELEVENLABS")); // set up our auth
        
        String[] fileNames = { "karuizawa.mp3" }; 
        makeVoice("KaruizawaChan", fileNames, "accent", "japanese");
        }

/* 
    public void start() throws ElevenLabsException {
        ElevenLabsAPI client = ElevenLabsAPI.getInstance();
        client.setAPIKey(System.getenv("ELEVENLABS"));

    }
    */
 /* 
    public static void printVoices() {
        // If we need them later, we save them
        try {
            voices = Voice.getVoices();
            voices.forEach(item -> {
                System.out.println(item.getName());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void makeVoice(String voiceName, String[] filePath, String labelName, String labelValue) {
        try {
            VoiceBuilder builder = new VoiceBuilder();
            builder.withName(voiceName);
            for (String item : filePath) {
                builder.withFile(new File(item));
            }
            builder.withLabel(labelName, labelValue);
            voice = builder.create();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
/* 
    public static String getVoiceIDByVoiceName(List<Voice> voices, String voiceName) {
        for (Voice voice : voices) {
            System.out.println(voice.getName());
            if (voice.getName().equals(voiceName))
                return voice.getVoiceId();
        }
        return null;
    }
    */
/* 
    public static void main(String[] args) throws ElevenLabsException {
        client = ElevenLabsAPI.getInstance(); // create client instance
        client.setAPIKey(System.getenv("ELEVENLABS")); // set up our auth
        
        String[] fileNames = { "karuizawa.mp3" }; 
        makeVoice("KaruizawaChan", fileNames, "accent", "japanese");
      //  printVoices();
        //voices = Voice.getVoices();
        //String voiceID = getVoiceIDByVoiceName(voices, "KaruizawaChan");

        //String voiceId = voiceID;

    }
*/
     
}
