package Server;


public class Twilio {
    // Find your Account SID and Auth Token at twilio.com/console
    // and set the environment variables. See http://twil.io/secure
    public static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    public static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");

    public static void main(String[] args) {
        //Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
       // Service service = Service.creator("My First Verify Service").create();

       // System.out.println(service.getSid());
    }
}

