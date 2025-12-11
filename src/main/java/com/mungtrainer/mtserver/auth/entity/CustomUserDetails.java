package com.mungtrainer.mtserver.auth.entity;


import com.mungtrainer.mtserver.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
  private final Long userId;
  private final String username;
  private final String password;
  private final String role;

  public CustomUserDetails(User user) {
    this.userId = user.getUserId();
    this.username = user.getUserName();
    this.password = user.getPassword();
    this.role = user.getRole();
  }

  public Long getUserId() {
    return userId;
  }

  public String getRole() {
    return role;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_"+role));
  }

  @Override
  public String getUsername() {
    return username;
  }
  @Override
  public String getPassword() {
    return password;
  }

  //
  @Override public boolean isAccountNonExpired() { return true; }
  @Override public boolean isAccountNonLocked() { return true; }
  @Override public boolean isCredentialsNonExpired() { return true; }
  @Override public boolean isEnabled() { return true; }
}
