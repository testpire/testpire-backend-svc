package com.testpire.testpire.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
@Data
public class CognitoConfig {

  @Value("${aws.cognito.region}")
  private String region;

  @Value("${aws.cognito.userPoolId}")
  private String userPoolId;

  @Value("${aws.cognito.clientId}")
  private String clientId;

  @Bean
  public CognitoIdentityProviderClient cognitoClient() {
    return CognitoIdentityProviderClient.builder()
        .region(Region.of(region))
        .build();
  }
}