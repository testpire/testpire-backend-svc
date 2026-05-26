package com.testpire.testpire.service;

import com.testpire.testpire.config.CognitoConfig;
import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.dto.UserDto;
import com.testpire.testpire.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CognitoService {

    @Autowired
    private CognitoIdentityProviderClient cognitoClient;

    @Autowired
    private CognitoConfig cognitoConfig;

    /**
     * Admin-creates a user in Cognito. Cognito generates a temporary password and
     * emails it to the user. The user status is set to FORCE_CHANGE_PASSWORD.
     * No password is required from the caller.
     */
    public String adminCreateUser(String username, String firstName, String lastName,
                                   UserRole role, Long instituteId) {
        try {
            AdminCreateUserRequest request = AdminCreateUserRequest.builder()
                    .userPoolId(cognitoConfig.getUserPoolId())
                    .username(username)
                    .userAttributes(
                            attr(ApplicationConstants.CognitoAttributes.EMAIL, username),
                            attr("email_verified", "true"),
                            attr(ApplicationConstants.CognitoAttributes.NAME, firstName + " " + lastName),
                            attr(ApplicationConstants.CognitoAttributes.CUSTOM_ROLE, role.name()),
                            attr(ApplicationConstants.CognitoAttributes.CUSTOM_INSTITUTE_ID, String.valueOf(instituteId)),
                            attr(ApplicationConstants.CognitoAttributes.PHONE_NUMBER, ApplicationConstants.Phone.DEFAULT_PHONE)
                    )
                    .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
                    .build();

            AdminCreateUserResponse response = cognitoClient.adminCreateUser(request);

            return response.user().attributes().stream()
                    .filter(a -> a.name().equals("sub"))
                    .findFirst()
                    .map(AttributeType::value)
                    .orElseThrow(() -> new RuntimeException("Could not get Cognito sub for user: " + username));

        } catch (Exception e) {
            log.error("Error creating user in Cognito: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating user: " + e.getMessage(), e);
        }
    }

    /**
     * Resends the invitation email with a new temporary password to a user
     * whose status is FORCE_CHANGE_PASSWORD.
     */
    public void resendInvitation(String username) {
        try {
            AdminCreateUserRequest request = AdminCreateUserRequest.builder()
                    .userPoolId(cognitoConfig.getUserPoolId())
                    .username(username)
                    .messageAction(MessageActionType.RESEND)
                    .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
                    .build();

            cognitoClient.adminCreateUser(request);
            log.info("Invitation resent to: {}", username);
        } catch (Exception e) {
            log.error("Error resending invitation: {}", e.getMessage(), e);
            throw new RuntimeException("Error resending invitation: " + e.getMessage(), e);
        }
    }

    /**
     * Authenticates a user. Returns a map with either:
     * - "token": the ID token on success
     * - "challengeName" + "session": if a NEW_PASSWORD_REQUIRED challenge is returned
     */
    public Map<String, String> login(String username, String password) {
        try {
            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", username);
            authParams.put("PASSWORD", password);

            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .clientId(cognitoConfig.getClientId())
                    .userPoolId(cognitoConfig.getUserPoolId())
                    .authParameters(authParams)
                    .build();

            AdminInitiateAuthResponse result = cognitoClient.adminInitiateAuth(authRequest);

            if (result.challengeName() != null) {
                log.info("Login challenge for {}: {}", username, result.challengeName());
                return Map.of(
                        "challengeName", result.challengeName().name(),
                        "session", result.session() != null ? result.session() : ""
                );
            }

            return Map.of("token", result.authenticationResult().idToken());

        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage(), e);
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    /**
     * Responds to a NEW_PASSWORD_REQUIRED challenge. Called after the user's first login
     * with the temporary password. Returns the final ID token.
     */
    public String setNewPassword(String username, String session, String newPassword) {
        try {
            Map<String, String> challengeResponses = new HashMap<>();
            challengeResponses.put("USERNAME", username);
            challengeResponses.put("NEW_PASSWORD", newPassword);

            RespondToAuthChallengeRequest request = RespondToAuthChallengeRequest.builder()
                    .clientId(cognitoConfig.getClientId())
                    .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                    .session(session)
                    .challengeResponses(challengeResponses)
                    .build();

            RespondToAuthChallengeResponse response = cognitoClient.respondToAuthChallenge(request);
            return response.authenticationResult().idToken();

        } catch (Exception e) {
            log.error("Set new password failed: {}", e.getMessage(), e);
            throw new RuntimeException("Set new password failed: " + e.getMessage(), e);
        }
    }

    /**
     * Initiates a forgot-password flow. Cognito sends a verification code to the user's email.
     */
    public void forgotPassword(String username) {
        try {
            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .clientId(cognitoConfig.getClientId())
                    .username(username)
                    .build();

            cognitoClient.forgotPassword(request);
            log.info("Forgot password initiated for: {}", username);
        } catch (Exception e) {
            log.error("Forgot password failed: {}", e.getMessage(), e);
            throw new RuntimeException("Forgot password failed: " + e.getMessage(), e);
        }
    }

    /**
     * Confirms a forgot-password reset. User provides the OTP from email and their new password.
     */
    public void confirmForgotPassword(String username, String confirmationCode, String newPassword) {
        try {
            ConfirmForgotPasswordRequest request = ConfirmForgotPasswordRequest.builder()
                    .clientId(cognitoConfig.getClientId())
                    .username(username)
                    .confirmationCode(confirmationCode)
                    .password(newPassword)
                    .build();

            cognitoClient.confirmForgotPassword(request);
            log.info("Password reset confirmed for: {}", username);
        } catch (Exception e) {
            log.error("Confirm forgot password failed: {}", e.getMessage(), e);
            throw new RuntimeException("Password reset failed: " + e.getMessage(), e);
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

    private AttributeType attr(String name, String value) {
        return AttributeType.builder().name(name).value(value).build();
    }

    private UserDto extractUserFromAttributes(String username, List<AttributeType> attributes) {
        String email = "";
        String firstName = "";
        String lastName = "";
        Long instituteId = -1L;
        UserRole role = UserRole.STUDENT;
        boolean enabled = true;

        for (AttributeType attr : attributes) {
            switch (attr.name()) {
                case ApplicationConstants.CognitoAttributes.EMAIL -> email = attr.value();
                case ApplicationConstants.CognitoAttributes.GIVEN_NAME -> firstName = attr.value();
                case ApplicationConstants.CognitoAttributes.FAMILY_NAME -> lastName = attr.value();
                case ApplicationConstants.CognitoAttributes.CUSTOM_ROLE -> role = UserRole.valueOf(attr.value());
                case ApplicationConstants.CognitoAttributes.CUSTOM_INSTITUTE_ID -> instituteId = Long.valueOf(attr.value());
                case "email_verified" -> enabled = Boolean.parseBoolean(attr.value());
            }
        }

        return new UserDto(username, email, firstName, lastName, role, enabled, instituteId);
    }
}
