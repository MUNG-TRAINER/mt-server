package com.mungtrainer.mtserver.common.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {

  public static Cookie createCookie(String name, String value, long maxAgeSeconds) {

    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);     // JS 접근 불가 → XSS 방어
    cookie.setSecure(false);       // true -> HTTPS에서만 전송
    cookie.setPath("/");          // 모든 경로에서 유효 우효
    cookie.setMaxAge((int) maxAgeSeconds);
    cookie.setAttribute("SameSite", "Lax"); // 또는 Lax (cross-site 여부에 따라 선택)

    return cookie;
  }

  public static Cookie deleteCookie(String name) {
    Cookie cookie = new Cookie(name, null);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(0); // 즉시 삭제
    cookie.setAttribute("SameSite", "Lax");

    return cookie;
  }
}
