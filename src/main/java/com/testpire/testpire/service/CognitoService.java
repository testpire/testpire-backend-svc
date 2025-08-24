package com.testpire.testpire.service;

import com.testpire.testpire.config.CognitoConfig;
import com.testpire.testpire.dto.RegisterRequest;
import com.testpire.testpire.dto.UserDto;
import com.testpire.testpire.enums.UserRole;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminConfirmSignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUserGlobalSignOutRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;

// CognitoService.java
@Service
@Slf4j
public class CognitoService {

  @Autowired
  private CognitoIdentityProviderClient cognitoClient;

  @Autowired
  private CognitoConfig cognitoConfig;

  public String signUp(RegisterRequest request, UserRole role) {
    try {
      AttributeType emailAttr = AttributeType.builder()
          .name("email")
          .value(request.email())
          .build();

      AttributeType givenNameAttr = AttributeType.builder()
          .name("given_name")
          .value(request.firstName())
          .build();

      AttributeType familyNameAttr = AttributeType.builder()
          .name("family_name")
          .value(request.lastName())
          .build();

      AttributeType customRoleAttr = AttributeType.builder()
          .name("custom:role")
          .value(role.name())
          .build();

      AttributeType instituteIdAttr = AttributeType.builder()
          .name("custom:instituteId")
          .value(request.instituteId())
          .build();

      AttributeType phone = AttributeType.builder()
          .name("phone_number")
          .value("+919905536608")
          .build();

      // Add SECRET_HASH to signup request
      Map<String, String> validationData = new HashMap<>();
      //validationData.put("SECRET_HASH", calculateSecretHash(request.username()));

      SignUpRequest signUpRequest = SignUpRequest.builder()
          .clientId(cognitoConfig.getClientId())
          .username(request.username())
          .password(request.password())
          .userAttributes(emailAttr, givenNameAttr, familyNameAttr, customRoleAttr, instituteIdAttr, phone)
          .build();

      SignUpResponse result = cognitoClient.signUp(signUpRequest);

      // Auto-confirm user for simplicity (remove this in production)
      adminConfirmSignUp(request.username());

      return result.userSub();

    } catch (Exception e) {
      log.error("Error signing up user: {}", e.getMessage(), e);
      throw new RuntimeException("Error signing up user: " + e.getMessage(), e);
    }
  }

  private void adminConfirmSignUp(String username) {
    try {
      AdminConfirmSignUpRequest confirmRequest = AdminConfirmSignUpRequest.builder()
          .userPoolId(cognitoConfig.getUserPoolId())
          .username(username)
          .build();

      cognitoClient.adminConfirmSignUp(confirmRequest);
    } catch (Exception e) {
      log.warn("Could not auto-confirm user: {}", e.getMessage());
    }
  }

  public String login(String username, String password) {
    try {
      Map<String, String> authParams = new HashMap<>();
      authParams.put("USERNAME", username);
      authParams.put("PASSWORD", password);
     // authParams.put("SECRET_HASH", calculateSecretHash(username));

      AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
          .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
          .clientId(cognitoConfig.getClientId())
          .userPoolId(cognitoConfig.getUserPoolId())
          .authParameters(authParams)
          .build();

      AdminInitiateAuthResponse result = cognitoClient.adminInitiateAuth(authRequest);
      return result.authenticationResult().idToken();

    } catch (Exception e) {
      log.error("Login failed: {}", e.getMessage(), e);
      throw new RuntimeException("Login failed: " + e.getMessage(), e);
    }
  }

  public void logout(String username) {
    try {
      AdminUserGlobalSignOutRequest signOutRequest = AdminUserGlobalSignOutRequest.builder()
          .username(username)
          .userPoolId(cognitoConfig.getUserPoolId())
          .build();

      cognitoClient.adminUserGlobalSignOut(signOutRequest);
    } catch (Exception e) {
      log.error("Logout failed: {}", e.getMessage(), e);
      throw new RuntimeException("Logout failed: " + e.getMessage(), e);
    }
  }

  public UserDto getUser(String username) {
    try {
      AdminGetUserRequest request = AdminGetUserRequest.builder()
          .username(username)
          .userPoolId(cognitoConfig.getUserPoolId())
          .build();

      AdminGetUserResponse result = cognitoClient.adminGetUser(request);

      return extractUserFromAttributes(result.username(), result.userAttributes());

    } catch (Exception e) {
      log.error("Error getting user: {}", e.getMessage(), e);
      throw new RuntimeException("Error getting user: " + e.getMessage(), e);
    }
  }

  private UserDto extractUserFromAttributes(String username, List<AttributeType> attributes) {
    String email = "";
    String firstName = "";
    String lastName = "";
    String instituteId = "";
    UserRole role = UserRole.STUDENT;
    boolean enabled = true;

    for (AttributeType attr : attributes) {
      switch (attr.name()) {
        case "email" -> email = attr.value();
        case "given_name" -> firstName = attr.value();
        case "family_name" -> lastName = attr.value();
        case "custom:role" -> role = UserRole.valueOf(attr.value());
        case "custom:instituteId" -> instituteId = attr.value();
        case "email_verified" -> enabled = Boolean.parseBoolean(attr.value());
      }
    }

    return new UserDto(username, email, firstName, lastName, role, enabled, instituteId);
  }

 /* private String calculateSecretHash(String username) {
    try {
      SecretKeySpec signingKey = new SecretKeySpec(
          cognitoConfig.getClientSecret().getBytes(StandardCharsets.UTF_8),
          "HmacSHA256"
      );

      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(signingKey);
      mac.update(username.getBytes(StandardCharsets.UTF_8));
      byte[] rawHmac = doFinal(cognitoConfig.getClientId().getBytes(StandardCharsets.UTF_8));

      return Base64.getEncoder().encodeToString(rawHmac);
    } catch (Exception e) {
      throw new RuntimeException("Error calculating secret hash", e);
    }
  }*/
}