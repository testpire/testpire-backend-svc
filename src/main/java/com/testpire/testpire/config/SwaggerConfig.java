package com.testpire.testpire.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

  /** Name of the bearer-JWT scheme; referenced by the global security requirement and the Authorize button. */
  private static final String BEARER_SCHEME = "bearerAuth";

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Testpire API")
            .version("1.0")
            .description("Online Test Platform API with AWS Cognito Integration. "
                + "Log in via POST /api/auth/login, copy the `accessToken` from the response, "
                + "click Authorize, and paste it to call the protected endpoints.")
            .contact(new Contact()
                .name("Testpire Team")
                .email("support@testpire.com")
            )
        )
        .servers(List.of(
            new Server().url("http://localhost:8080").description("Development server"),
            new Server().url("https://api.testpire.com").description("Production server")
        ))
        // Adds the "Authorize" button. Paste the Cognito accessToken (no "Bearer " prefix);
        // Swagger UI then sends `Authorization: Bearer <token>` on every request.
        .components(new Components().addSecuritySchemes(BEARER_SCHEME,
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste the accessToken returned by /api/auth/login")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
  }

}