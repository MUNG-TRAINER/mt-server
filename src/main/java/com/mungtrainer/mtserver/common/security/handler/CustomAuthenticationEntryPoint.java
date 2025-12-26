package com.mungtrainer.mtserver.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.common.security.AuthErrorResponse;
import com.mungtrainer.mtserver.common.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper;

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

    if(response.isCommitted()) return;

    String access = jwtTokenProvider.resolveAccessToken(request);
    String refresh = jwtTokenProvider.resolveRefreshToken(request);

    String code;
    String message;

    String uri = request.getRequestURI();

    if ("/api/auth/check".equals(uri)) {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write(
          objectMapper.writeValueAsString(
              AuthErrorResponse.builder()
                               .code("ANONYMOUS")
                               .message("로그인되어 있지 않습니다.")
                               .build()
                                         )
                                );
      response.getWriter().flush();
      return;
    }

    if (access == null && refresh != null) {
      if(jwtTokenProvider.validateToken(refresh, JwtTokenProvider.TokenType.REFRESH)){
        code = ErrorCode.TOKEN_EXPIRED.name();
        message = ErrorCode.TOKEN_EXPIRED.getMessage();
      }else{
        code= ErrorCode.REFRESH_EXPIRED.name();
        message = ErrorCode.REFRESH_EXPIRED.getMessage();
      }
    } else {
      code = ErrorCode.UNAUTHORIZED.name();
      message = ErrorCode.UNAUTHORIZED.getMessage();
    }
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(
        objectMapper.writeValueAsString(
            AuthErrorResponse.builder()
                             .code(code)
                             .message(message)
                             .build())
    );
  }

}
