package com.testpire.testpire.config;

import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.util.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(authz -> authz
            .requestMatchers(
                "/",
                "/index.html",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/v3/api-docs.yaml",
                "/v3/api-docs",
                "/api-docs/**",
                "/webjars/**",
                "/swagger-resources/**",
                "/swagger-resources",
                "/configuration/ui",
                "/configuration/security",
                "/favicon.ico",
                "/error",
                "/api/auth/**"
            ).permitAll()
            .requestMatchers("/api/institute/**").hasRole(UserRole.SUPER_ADMIN.name())
            .requestMatchers("/api/super-admin/**").hasRole(UserRole.SUPER_ADMIN.name())
            .requestMatchers("/api/users/**").hasAnyRole(UserRole.SUPER_ADMIN.name(), UserRole.INST_ADMIN.name(), UserRole.TEACHER.name())
            .requestMatchers("/api/teacher/**").hasRole(UserRole.TEACHER.name())
            .requestMatchers("/api/student/**").hasRole(UserRole.STUDENT.name())
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}