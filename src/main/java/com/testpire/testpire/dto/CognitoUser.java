package com.testpire.testpire.dto;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

// CognitoUser.java
public class CognitoUser extends User {
  private final String firstName;
  private final String lastName;
  private final String email;
  private final String instituteId;

  public CognitoUser(String username, String password,
      Collection<? extends GrantedAuthority> authorities,
      String firstName, String lastName, String email, boolean enabled, String instituteId) {
    super(username, password, enabled, true, true, true, authorities);
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.instituteId = instituteId;
  }

  // Getters
  public String getFirstName() { return firstName; }
  public String getLastName() { return lastName; }
  public String getEmail() { return email; }
  public String getInstituteId() { return instituteId; }
  public String getFullName() { return firstName + " " + lastName; }
}