package com.mungtrainer.mtserver.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.common.security.AuthErrorResponse;
import com.mungtrainer.mtserver.common.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.manager.StatusManagerServlet;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final JwtTokenProvider jwtTokenProvider;

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

    String access = jwtTokenProvider.resolveAccessToken(request);
    String refresh = jwtTokenProvider.resolveRefreshToken(request);

    String code;
    String message;

    if (access == null && refresh != null) {
      code = ErrorCode.TOKEN_EXPIRED.name();
      message = ErrorCode.TOKEN_EXPIRED.getMessage();
    } else {
      code = ErrorCode.UNAUTHORIZE.name();
      message = ErrorCode.UNAUTHORIZE.getMessage();
    }
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(
        new ObjectMapper().writeValueAsString(
            AuthErrorResponse.builder()
                             .code(code)
                             .message(message)
                             .build())
    );

  }

}
