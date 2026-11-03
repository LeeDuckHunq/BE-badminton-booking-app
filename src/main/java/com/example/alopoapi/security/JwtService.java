package com.example.alopoapi.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final byte[] signerKey;
    private final long expirationHours;

    public JwtService(
            @Value("${app.jwt.secret}") String signerKey,
            @Value("${app.jwt.expiration-hours:12}") long expirationHours
    ) {
        this.signerKey = signerKey.getBytes(StandardCharsets.UTF_8);
        this.expirationHours = expirationHours;
        if (this.signerKey.length < 64) {
            throw new IllegalStateException("JWT_SECRET must contain at least 64 characters");
        }
    }

    public String generateToken(String username, String role) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim("role", role)
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plus(expirationHours, ChronoUnit.HOURS)))
                    .build();

            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
            jwt.sign(new MACSigner(signerKey));
            return jwt.serialize();
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot generate access token", exception);
        }
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(signerKey);
            Date expiresAt = jwt.getJWTClaimsSet().getExpirationTime();
            return jwt.verify(verifier) && expiresAt != null && expiresAt.after(new Date());
        } catch (Exception exception) {
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid access token", exception);
        }
    }

    public String extractRole(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getStringClaim("role");
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid access token", exception);
        }
    }
}
