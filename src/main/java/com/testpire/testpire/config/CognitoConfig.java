package com.testpire.testpire.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.identity.spi.internal.DefaultAwsCredentialsIdentity;
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

  @Value("${aws.accessKeyId}")
  private String accessKeyId;

  @Value("${aws.secretKey}")
  private String secretKey;

  @Bean
  public CognitoIdentityProviderClient cognitoClient() {
    AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretKey);
    return CognitoIdentityProviderClient.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
        .build();
  }
}