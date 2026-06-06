package com.testpire.testpire.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
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

  /*@Bean
  public FlywayMigrationStrategy flywayMigrationStrategy() {
    return flyway -> {
      flyway.repair();   // realign stored checksums with the files
      flyway.migrate();
    };
  }*/

  @Bean
  public CognitoIdentityProviderClient cognitoClient(AwsCredentialsProvider awsCredentialsProvider) {
    return CognitoIdentityProviderClient.builder()
        .region(Region.of(region))
        .credentialsProvider(awsCredentialsProvider)
        .build();
  }
}