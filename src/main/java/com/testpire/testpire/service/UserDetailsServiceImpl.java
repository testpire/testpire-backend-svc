package com.testpire.testpire.service;

import com.testpire.testpire.config.CognitoConfig;
import com.testpire.testpire.dto.CognitoUser;
import com.testpire.testpire.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private CognitoService cognitoService;

  @Autowired
  private CognitoConfig cognitoConfig;

  @Autowired
  private CognitoIdentityProviderClient cognitoClient;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    try {
      UserDto user = cognitoService.getUser(username);

      return new CognitoUser(
          user.username(),
          "",
          user.role().getAuthorities(),
          user.firstName(),
          user.lastName(),
          user.email(),
          user.enabled(),
          user.instituteId()
      );

    } catch (Exception e) {
      log.error("User not found: {}", username, e);
      throw new UsernameNotFoundException("User not found: " + username, e);
    }
  }
}
