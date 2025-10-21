package org.example;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KeycloakAuth {



    public static String getTokenUser(String code, String redirectUri, String clientId, String clientSecret) throws IOException, InterruptedException {

        String urlString = Config.BASE_URL + "/realms/" + Config.KEYCLOAK_REALM + "/protocol/openid-connect/token"; // change to use HTTPS, can't use localhost in production

        //authorization code flow used for users
        String data = "grant_type=authorization_code"
                + "&code=" + code
                + "&redirect_uri=" + redirectUri
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret;

        return requestToken(urlString, data);
    }

    private static String requestToken(String urlString, String data) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200) {
            throw new IOException("Failed to authenticate: " + response.statusCode() + " " + response.body());
        }
        JSONObject json = new JSONObject(response.body());
        return json.getString("access_token");
    }








}
