package com.mungtrainer.mtserver.auth.service;

import com.mungtrainer.mtserver.auth.dao.AuthDAO;
import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final AuthDAO authDAO;

  @Override
  public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
    // username 기준 로그인 (userName 또는 email로 바꿀 수 있음)
    User user = authDAO.findByUserName(userName);
    if (user == null) {
      throw new UsernameNotFoundException("Not found:" + userName);
    }
    return new CustomUserDetails(user);
  }
}
