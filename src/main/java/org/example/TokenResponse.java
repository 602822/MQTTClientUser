package org.example;

public class TokenResponse {
    public final String accessToken;
    public final String refreshToken;
    public final long expiresIn;

    public TokenResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

}
