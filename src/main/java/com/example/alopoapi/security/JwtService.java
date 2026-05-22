/*
package com.example.alopoapi.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private static final String SIGNER_KEY =
            "3014b2ac9b47cb209f92424c3bb2f7b6f154fcea6f9886598c19478324aedbc5";

    // Generate Token
    public String generateToken(String username, String role) {

        try {

            JWSHeader header =
                    new JWSHeader(JWSAlgorithm.HS512);

            JWTClaimsSet claimsSet =
                    new JWTClaimsSet.Builder()
                            .subject(username)
                            .claim("role", role)
                            .issueTime(new Date())
                            .expirationTime(
                                    new Date(
                                            Instant.now()
                                                    .plus(1, ChronoUnit.HOURS)
                                                    .toEpochMilli()
                                    )
                            )
                            .build();

            Payload payload =
                    new Payload(claimsSet.toJSONObject());

            JWSObject jwsObject =
                    new JWSObject(header, payload);

            jwsObject.sign(
                    new MACSigner(SIGNER_KEY.getBytes())
            );

            return jwsObject.serialize();

        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    // Validate Token
    public boolean validateToken(String token) {

        try {

            SignedJWT signedJWT =
                    SignedJWT.parse(token);

            JWSVerifier verifier =
                    new MACVerifier(
                            SIGNER_KEY.getBytes()
                    );

            boolean verified =
                    signedJWT.verify(verifier);

            Date expirationTime =
                    signedJWT.getJWTClaimsSet()
                            .getExpirationTime();

            return verified &&
                    expirationTime.after(new Date());

        } catch (Exception e) {
            return false;
        }
    }

    // Extract Username
    public String extractUsername(String token) {

        try {

            SignedJWT signedJWT =
                    SignedJWT.parse(token);

            return signedJWT
                    .getJWTClaimsSet()
                    .getSubject();

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    // Extract Role
    public String extractRole(String token) {

        try {

            SignedJWT signedJWT =
                    SignedJWT.parse(token);

            return signedJWT
                    .getJWTClaimsSet()
                    .getStringClaim("role");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

 */
