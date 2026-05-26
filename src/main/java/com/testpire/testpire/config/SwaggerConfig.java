package com.testpire.testpire.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Testpire API")
            .version("1.0")
            .description("Online Test Platform API with AWS Cognito Integration")
            .contact(new Contact()
                .name("Testpire Team")
                .email("support@testpire.com")
            )
        )
        .servers(List.of(
            new Server().url("http://localhost:8080").description("Development server"),
            new Server().url("https://api.testpire.com").description("Production server")
        ))
        .components(new Components());
  }

}