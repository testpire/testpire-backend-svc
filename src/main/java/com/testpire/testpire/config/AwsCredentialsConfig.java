package com.testpire.testpire.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Configuration
public class AwsCredentialsConfig {

  /**
   * Shared credentials provider for all AWS SDK clients (Cognito, S3).
   * <p>
   * If {@code aws.access-key-id} / {@code aws.secret-access-key} are set (e.g. in
   * application.properties for local dev), those static keys are used. Otherwise we
   * fall back to the default chain (env vars, {@code ~/.aws} profile, instance role).
   * <p>
   * The property names map to the {@code AWS_ACCESS_KEY_ID} / {@code AWS_SECRET_ACCESS_KEY}
   * env vars via Spring relaxed binding, so the Lightsail/EC2 container keeps using the
   * env vars it is given — they override the values baked into the jar.
   */
  @Bean
  public AwsCredentialsProvider awsCredentialsProvider(
      @Value("${aws.access-key-id:}") String accessKeyId,
      @Value("${aws.secret-access-key:}") String secretAccessKey) {
    if (!accessKeyId.isBlank() && !secretAccessKey.isBlank()) {
      return StaticCredentialsProvider.create(
          AwsBasicCredentials.create(accessKeyId, secretAccessKey));
    }
    return DefaultCredentialsProvider.create();
  }
}
