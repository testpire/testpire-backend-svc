package com.testpire.testpire.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtil {

  @Value("${aws.cognito.userPoolId}")
  private String userPoolId;

  @Value("${aws.cognito.clientId:cpojsnho1d17v1bh7lkjnbmgf}")
  private String appClientId;

  @Value("${aws.cognito.region}")
  private String region;

  private final CognitoJwtParser cognitoJwtParser;

  public JwtUtil(CognitoJwtParser cognitoJwtParser) {
    this.cognitoJwtParser = cognitoJwtParser;
  }

  public String extractCognitoUserId(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public String extractUsername(String token) {
    Claims claims = extractAllClaims(token);
    log.info("JWT Claims: {}", claims);
    
    // Try to get username from 'cognito:username' claim first, then fallback to 'sub'
    String username = claims.get("cognito:username", String.class);
    if (username == null) {
      username = claims.getSubject();
    }
    
    log.info("Extracted username from JWT: {}", username);
    return username;
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(cognitoJwtParser.getPublicKey(userPoolId, region))
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public boolean isTokenValid(String token) {
    try {
      Claims claims = extractAllClaims(token);
      log.info("claims : {}", claims);
      // Additional validation for Cognito tokens
      return isTokenExpired(claims) &&
          isValidIssuer(claims) &&
          isValidAudience(claims);
    } catch (Exception e) {
      log.error("Error validating token ", e);
      return false;
    }
  }

  private boolean isTokenExpired(Claims claims) {
    boolean isExpired =  claims.getExpiration().after(new Date());
    log.info("is token expired :{} expiration :{} ", isExpired,  claims.getExpiration());
    return isExpired;
  }

  private boolean isValidIssuer(Claims claims) {
    String issuer = claims.getIssuer();
    log.info("is valid issuer :{} ", issuer);
    return issuer != null && issuer.equals("https://cognito-idp." + region + ".amazonaws.com/" + userPoolId);
  }

  private boolean isValidAudience(Claims claims) {
    String audience = claims.getAudience();
    log.info("is valid audience :{} ", audience);
    // Replace with your actual app client ID
    return audience != null && audience.equals(appClientId);
  }

}