package com.testpire.testpire.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class CognitoJwtParser {

  private static final String COGNITO_KEYS_URL = "https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json";

  public PublicKey getPublicKey(String userPoolId, String region) {
    try {
      String url = String.format(COGNITO_KEYS_URL, region, userPoolId);
      Map<String, Object> jwks = fetchJwks(url);

      List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
      if (keys == null || keys.isEmpty()) {
        throw new RuntimeException("No keys found in JWKS");
      }

      // Get the first key (for production, you should match kid from token header)
      Map<String, Object> key = keys.get(0);
      return generatePublicKey(key);
    } catch (Exception e) {
      throw new RuntimeException("Failed to get Cognito public key", e);
    }
  }

  private Map<String, Object> fetchJwks(String url) throws Exception {
    try (InputStream is = new URL(url).openStream()) {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(is, Map.class);
    }
  }

  private PublicKey generatePublicKey(Map<String, Object> key) throws Exception {
    String keyType = (String) key.get("kty");
    if (!"RSA".equals(keyType)) {
      throw new RuntimeException("Invalid key type: " + keyType);
    }

    String modulus = (String) key.get("n");
    String exponent = (String) key.get("e");

    byte[] modBytes = Base64.getUrlDecoder().decode(modulus);
    byte[] expBytes = Base64.getUrlDecoder().decode(exponent);

    java.math.BigInteger modulusBigInt = new java.math.BigInteger(1, modBytes);
    java.math.BigInteger exponentBigInt = new java.math.BigInteger(1, expBytes);

    java.security.spec.RSAPublicKeySpec keySpec =
        new java.security.spec.RSAPublicKeySpec(modulusBigInt, exponentBigInt);

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePublic(keySpec);
  }
}
