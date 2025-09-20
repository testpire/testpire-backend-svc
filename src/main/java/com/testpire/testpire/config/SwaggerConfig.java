package com.testpire.testpire.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT")
            )
        )
        .servers(List.of(
            new Server().url("http://localhost:8080").description("Development server"),
            new Server().url("https://api.testpire.com").description("Production server")
        ))
        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
        .components(new Components()
            .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme())
        );
  }

  private SecurityScheme createAPIKeyScheme() {
    return new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .bearerFormat("JWT")
        .scheme("bearer");
  }
}