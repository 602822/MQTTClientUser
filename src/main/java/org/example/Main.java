package org.example;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, ParseException {

        //loads sensitive data from properties file
        Properties props = ConfigLoader.load();

        //proves that the auth code is coming from a legit application and not an imposter
        String clientId = props.getProperty("CLIENT_ID");
        String redirectUri = props.getProperty("REDIRECT_URI");

        String codeVerifier = PKCEUtils.generateCodeVerifier();
        String codeChallenge = PKCEUtils.generateCodeChallenge(codeVerifier);
        String authUrl = PKCEUtils.generateAuthURL(clientId, redirectUri, codeChallenge);

        System.out.println("Opening browser for Keycloak login...");
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI.create(authUrl));
        } else {
            System.out.println("Please open the following URL in your browser to authenticate:");
            System.out.println(authUrl);
        }

        //Retrieves the authorization code asynchronously once the user has authenticated
        CompletableFuture<String> codeFuture = AuthServer.waitForAuthCode(8081);
        String authCode = codeFuture.join();
        System.out.println("Received auth code: " + authCode);

        // Keycloak compares the codeVerifier with the previously sent codeChallenge to ensure they match
        // Only issues token if they match
        TokenResponse tokenResponse = KeycloakAuth.getTokenUserPKCE(authCode, redirectUri, clientId, codeVerifier);
        String jwtToken = tokenResponse.accessToken();
        System.out.println("JWT Token: " + jwtToken);

        MQTTSubClient subscriberClient = new MQTTSubClient(Config.BROKER_URL, clientId);

        //Extracting location and provider from jwt to construct the topic
        SignedJWT signedJWT = SignedJWT.parse(jwtToken);
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        String location = claimsSet.getStringClaim("location");
        String provider = claimsSet.getStringClaim("provider");
        String topic = String.format("smartocean/%s/%s/+/temperature", location, provider);

        subscriberClient.connect(jwtToken);
        subscriberClient.subscribe(topic);

        System.out.println("User is now subscribed to temperature data, waiting for messages...");

        //Keeps the main thread alive, preventing the JVM from shutting down
        //so the client can continue receiving messages
        while (true) {
            try {
                Thread.sleep(1000);

                //Check if the token is about to expire in the next 60 seconds
                //Refresh it if needed
                if (JWTUtils.willExpireSoon(jwtToken, 60)) {
                    System.out.println("JWT is about to expire, refreshing...");
                    tokenResponse = KeycloakAuth.refreshToken(tokenResponse.refreshToken(), clientId);
                    jwtToken = tokenResponse.accessToken();
                    subscriberClient.refreshConnection(jwtToken); //reconnect with new token
                    subscriberClient.subscribe(topic);
                }

            } catch (InterruptedException e) {
                System.err.println("Main thread interrupted: " + e.getMessage());
                break;
            }
        }


    }
}