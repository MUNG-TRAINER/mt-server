package com.mungtrainer.mtserver.common.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  /**
   * 1) JWT 인증 실패 처리
   * - JwtAuthenticationFilter에서 발생하는 AuthenticationException 처리
   */
  @Override
  public void commence(HttpServletRequest request,
                       HttpServletResponse response,
                       AuthenticationException authException) throws IOException {


    log.warn("Authentication failed at {} - reason: {}",
             request.getRequestURI(),
             authException.getMessage());

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write("""
                               {
                                 "status": 401,
                                 "error": "UNAUTHORIZED",
                                 "message": "토큰이 유효하지 않거나 만료되었습니다."
                               }
                               """);
  }

}
