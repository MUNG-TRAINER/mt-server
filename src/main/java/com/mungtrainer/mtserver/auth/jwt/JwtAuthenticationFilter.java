package com.mungtrainer.mtserver.auth.jwt;

import com.mungtrainer.mtserver.auth.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService customUserDetailsService;

  private static final String ACCESS_TOKEN = "access_token";

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    return path.startsWith("/api/auth")
           || path.equals("/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    String token = extractAccessTokenFromCookie(request);

    if (token != null &&
        jwtTokenProvider.validateToken(token,JwtTokenProvider.TokenType.ACCESS) &&
        SecurityContextHolder.getContext().getAuthentication() == null) {
      String username = jwtTokenProvider.getUsername(token, JwtTokenProvider.TokenType.ACCESS);
      UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
          );
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }
  private String extractAccessTokenFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();

    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (ACCESS_TOKEN.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
