package com.testpire.testpire.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testpire.testpire.dto.UserDto;
import com.testpire.testpire.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
public class JwksJwtUtil {

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId:cpojsnho1d17v1bh7lkjnbmgf}")
    private String appClientId;

    @Value("${aws.cognito.region}")
    private String region;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, PublicKey> publicKeys;

    @PostConstruct
    public void init() {
        loadJwksKeys();
    }

    private void loadJwksKeys() {
        try {
            ClassPathResource resource = new ClassPathResource("jwks.json");
            InputStream inputStream = resource.getInputStream();
            String jwksJson = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            
            JsonNode jwks = objectMapper.readTree(jwksJson);
            JsonNode keys = jwks.get("keys");
            
            publicKeys = new java.util.HashMap<>();
            
            for (JsonNode key : keys) {
                String kid = key.get("kid").asText();
                String n = key.get("n").asText();
                String e = key.get("e").asText();
                
                PublicKey publicKey = createPublicKey(n, e);
                publicKeys.put(kid, publicKey);
                
                log.info("Loaded public key with kid: {}", kid);
            }
            
            log.info("Successfully loaded {} public keys from JWKS", publicKeys.size());
            
        } catch (IOException e) {
            log.error("Failed to load JWKS file", e);
            throw new RuntimeException("Failed to load JWKS file", e);
        }
    }

    private PublicKey createPublicKey(String n, String e) {
        try {
            byte[] nBytes = Base64.getUrlDecoder().decode(n);
            byte[] eBytes = Base64.getUrlDecoder().decode(e);
            
            BigInteger modulus = new BigInteger(1, nBytes);
            BigInteger exponent = new BigInteger(1, eBytes);
            
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
            
        } catch (Exception ex) {
            log.error("Failed to create public key", ex);
            throw new RuntimeException("Failed to create public key", ex);
        }
    }

    public String extractUsername(String token) {
        try {
            Claims claims = extractAllClaims(token);
            
            // Try to get username from 'cognito:username' claim first, then fallback to 'sub'
            String username = claims.get("cognito:username", String.class);
            if (username == null) {
                username = claims.getSubject();
            }
            
            log.info("Extracted username from JWT: {}", username);
            return username;
        } catch (Exception e) {
            log.error("Error extracting username from token", e);
            throw new RuntimeException("Failed to extract username from JWT token", e);
        }
    }

    public boolean isTokenValid(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                log.error("Token is null or empty");
                return false;
            }

            // Check token format
            String[] chunks = token.split("\\.");
            if (chunks.length != 3) {
                log.error("Invalid JWT token format");
                return false;
            }

            // Extract and validate claims
            Claims claims = extractAllClaims(token);
            
            // Additional validation (expiration is already checked in extractAllClaims)
            // Check issuer
            String issuer = claims.getIssuer();
            String expectedIssuer = "https://cognito-idp." + region + ".amazonaws.com/" + userPoolId;
            if (issuer == null || !issuer.equals(expectedIssuer)) {
                log.error("Invalid issuer: {} (expected: {})", issuer, expectedIssuer);
                return false;
            }
            
            // Check audience
            String audience = claims.getAudience();
            if (audience == null || !audience.equals(appClientId)) {
                log.error("Invalid audience: {} (expected: {})", audience, appClientId);
                return false;
            }
            
            log.info("Token validation successful");
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("Token has expired: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage(), e);
            return false;
        }
    }

    public UserDto extractUserFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            
            // Extract user information
            String username = claims.get("cognito:username", String.class);
            if (username == null) {
                username = claims.getSubject();
            }
            
            String email = claims.get("email", String.class);
            String firstName = claims.get("given_name", String.class);
            String lastName = claims.get("family_name", String.class);
            String roleStr = claims.get("custom:roles", String.class);
            String instituteIdStr = claims.get("custom:instituteId", String.class);
            
            // Parse role
            UserRole role = UserRole.STUDENT; // default
            if (roleStr != null) {
                try {
                    role = UserRole.valueOf(roleStr);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid role in token: {}, using default STUDENT", roleStr);
                }
            }
            
            // Parse instituteId
            Long instituteId = null;
            if (instituteIdStr != null && !instituteIdStr.isEmpty()) {
                try {
                    instituteId = Long.valueOf(instituteIdStr);
                } catch (NumberFormatException e) {
                    log.warn("Invalid instituteId in token: {}, using null", instituteIdStr);
                }
            }
            
            log.info("Extracted user from token: username={}, role={}, email={}, instituteId={}", username, role, email, instituteId);
            
            return new UserDto(username, email, firstName, lastName, role, true, instituteId);
        } catch (Exception e) {
            log.error("Error extracting user from token", e);
            throw new RuntimeException("Failed to extract user from JWT token", e);
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            // Extract header to get kid
            String[] chunks = token.split("\\.");
            String header = new String(Base64.getUrlDecoder().decode(chunks[0]));
            
            JsonNode headerJson = objectMapper.readTree(header);
            String kid = headerJson.get("kid").asText();
            
            // Get the public key for this kid
            PublicKey publicKey = publicKeys.get(kid);
            if (publicKey == null) {
                throw new RuntimeException("No public key found for kid: " + kid);
            }
            
            // Parse and verify the token with clock skew tolerance
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .setAllowedClockSkewSeconds(300) // 5 minutes tolerance
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                    
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("JWT token has expired: {}", e.getMessage());
            throw new RuntimeException("JWT token has expired. Please login again.", e);
        } catch (io.jsonwebtoken.SignatureException e) {
            log.error("JWT signature verification failed: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT signature", e);
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.error("Malformed JWT token: {}", e.getMessage());
            throw new RuntimeException("Malformed JWT token", e);
        } catch (Exception e) {
            log.error("Error extracting claims from token", e);
            throw new RuntimeException("Failed to extract claims from JWT token: " + e.getMessage(), e);
        }
    }
}
