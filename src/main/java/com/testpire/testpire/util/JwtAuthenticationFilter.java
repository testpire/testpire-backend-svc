package com.testpire.testpire.util;

import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.service.CognitoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// JwtAuthenticationFilter.java
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;
  private final CognitoService cognitoService;


  public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService,
      CognitoService cognitoService) {
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
    this.cognitoService = cognitoService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    final String authHeader = request.getHeader(ApplicationConstants.Headers.AUTHORIZATION);

    if (authHeader == null || !authHeader.startsWith(ApplicationConstants.Headers.BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    final String jwt = authHeader.substring(ApplicationConstants.Headers.BEARER_PREFIX.length());
    log.info("extracted jwt : {}", jwt);
    final String username = jwtUtil.extractUsername(jwt);

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      log.info("extracted userDetails : {}", userDetails);
      if (jwtUtil.isTokenValid(jwt)) {
        log.info("valid token");
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }

    filterChain.doFilter(request, response);
  }
}