package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        //comes from OAuth redirect after login
        String authCode = "authorization_code_here";
        String redirectUri = Config.REDIRECT_URI;

        String clientId = "your_client_id";
        String clientSecret = "your_client_secret";

        String jwtToken = KeycloakAuth.getTokenUser(authCode, redirectUri, clientId, clientSecret);
        MQTTSubClient subscriberClient = new MQTTSubClient(Config.BROKER_URL, clientId);

        subscriberClient.connect(jwtToken);
        subscriberClient.subscribe("sensors/+/temperature");

        System.out.println("User is now subscribed to temperature data, waiting for messages...");

        //Keeps the main thread alive, preventing the JVM from shutting down
        //so the client can continue receiving messages
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Main thread interrupted: " + e.getMessage());
                break;
            }
        }





    }
}