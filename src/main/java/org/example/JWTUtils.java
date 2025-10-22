package org.example;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

public class JWTUtils {

    public static Instant getExpirationTime(String jwtToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(jwtToken);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date expirationDate = claims.getExpirationTime();
            if(expirationDate == null) {
                throw new IllegalArgumentException("JWT token does not contain an expiration time.");
            }
            return expirationDate.toInstant();

        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse JWT token",e);
        }
    }

    public static boolean willExpireSoon(String jwtToken, long secondsThreshold) {
        Instant expirationTime = getExpirationTime(jwtToken);
        return Instant.now().isAfter(expirationTime.minusSeconds(secondsThreshold));
    }




}
