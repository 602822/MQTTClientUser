package org.example;

import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        //comes from OAuth redirect after login
        String authCode = "authorization_code_here";

        //loads sensitive data from properties file
        Properties props = ConfigLoader.load();

        String redirectUri = props.getProperty("redirect_uri");

        //proves that the auth code is coming from a legit application and not an imposter that intercepted the code
        String clientId = props.getProperty("client_id");
        String clientSecret = props.getProperty("client_secret");


        TokenResponse tokenResponse = KeycloakAuth.getTokenUser(authCode, redirectUri, clientId, clientSecret);
        String jwtToken = tokenResponse.accessToken();

        MQTTSubClient subscriberClient = new MQTTSubClient(Config.BROKER_URL, clientId);

        subscriberClient.connect(jwtToken);
        subscriberClient.subscribe("sensors/+/temperature");

        System.out.println("User is now subscribed to temperature data, waiting for messages...");

        //Keeps the main thread alive, preventing the JVM from shutting down
        //so the client can continue receiving messages
        while (true) {
            try {
                Thread.sleep(1000);
                if (JWTUtils.willExpireSoon(jwtToken, 60)) {
                    System.out.println("JWT is about to expire, refreshing...");
                    tokenResponse = KeycloakAuth.refreshToken(tokenResponse.refreshToken(), clientId, clientSecret);
                    jwtToken = tokenResponse.accessToken();
                    subscriberClient.refreshConnection(jwtToken); //reconnect with new token
                }

            } catch (InterruptedException e) {
                System.err.println("Main thread interrupted: " + e.getMessage());
                break;
            }
        }


    }
}