package com.mungtrainer.mtserver.common.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;


@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(HttpServletRequest request,
                     HttpServletResponse response,
                     AccessDeniedException accessDeniedException) throws IOException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
    response.setContentType("application/json;charset=UTF-8");
    log.warn("Access Denied Exception");
    String body = """
                {
                    "success": false,
                    "status": 403,
                    "message": "접근 권한이 없습니다."
                }
                """;

    response.getWriter().write(body);
  }
}
