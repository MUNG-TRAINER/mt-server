package com.mungtrainer.mtserver.common.security;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {
  private final String atSecret;
  private final String rtSecret;
  private final long accessTokenValidityInMs;
  private final long refreshTokenValidityInMs;
  private Key atKey;
  private Key rtKey;

  public enum TokenType {
    ACCESS, REFRESH
  }

  public JwtTokenProvider(
      @Value("${spring.jwt.access-secret}") String atSecret,
      @Value("${spring.jwt.refresh-secret}") String rtSecret,
      @Value("${spring.jwt.access-token-validity-in-ms}") long accessTokenValidityInMs,
      @Value("${spring.jwt.refresh-token-validity-in-ms}") long refreshTokenValidityInMs
      ) {
    this.atSecret = atSecret;
    this.rtSecret = rtSecret;
    this.accessTokenValidityInMs = accessTokenValidityInMs;
    this.refreshTokenValidityInMs = refreshTokenValidityInMs;
  }

  @PostConstruct
  public void init() {
    // 시크릿 문자열을 HMAC-SHA 키로 변환
    this.atKey = Keys.hmacShaKeyFor(atSecret.getBytes(StandardCharsets.UTF_8));
    this.rtKey = Keys.hmacShaKeyFor(rtSecret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(Authentication authentication) {
    CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
    return buildToken(principal, TokenType.ACCESS, accessTokenValidityInMs);
  }

  public String generateRefreshToken(Authentication authentication) {
    CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
    return buildToken(principal, TokenType.REFRESH, refreshTokenValidityInMs);
  }

  /**
   * 주어진 사용자 정보와 토큰 타입, 유효기간을 기반으로 JWT 토큰을 생성합니다.
   *
   * @param principal   토큰에 포함될 사용자 정보(CustomUserDetails)
   * @param tokenType   생성할 토큰의 타입(ACCESS 또는 REFRESH)
   * @param validityInMs 토큰의 유효기간(밀리초 단위)
   * @return 생성된 JWT 토큰 문자열
   */
  public String buildToken(CustomUserDetails principal, TokenType tokenType, long validityInMs) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + validityInMs);

    return Jwts.builder()
               .subject(principal.getUsername())
               .claim("uid", principal.getUserId())
               .claim("role", principal.getRole())
               .claim("type", tokenType.name())
               .id(UUID.randomUUID().toString())
               .issuedAt(now)
               .expiration(expiry)
               .signWith(tokenType == TokenType.ACCESS ? atKey : rtKey)
               .compact();
  }

  public String getUsername(String token, TokenType tokenType) {
    return parseClaims(token, tokenType)
        .getPayload()
        .getSubject();
  }

  public Long getUserId(String token, TokenType tokenType) {
    Claims claims = parseClaims(token, tokenType).getPayload();
    return claims.get("uid", Long.class);
  }

  public String getRole(String token, TokenType tokenType) {
    Claims claims = parseClaims(token, tokenType).getPayload();
    return claims.get("role", String.class);
  }

  public TokenType getTokenType(String token, TokenType tokenType) {
    Claims claims = parseClaims(token, tokenType).getPayload();
    String type = claims.get("type", String.class);
    return type != null ? TokenType.valueOf(type) : null;
  }
  /**
   * 주어진 JWT 토큰의 유효성을 검증합니다.
   *
   * @param token      검증할 JWT 토큰 문자열
   * @param tokenType  토큰 타입(ACCESS 또는 REFRESH)
   * @return           토큰이 유효하면 true, 만료되었거나 변조 등으로 유효하지 않으면 false
   *                   (파싱 실패, 만료, 서명 오류 등 모든 예외 상황에서 false 반환)
   */
  public boolean validateToken(String token, TokenType tokenType) {
    try {
      Jws<Claims> jws = parseClaims(token, tokenType);
      Date expiration = jws.getPayload().getExpiration();
      return expiration.after(new Date());
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * 내부에서만 사용하는 Claims 파서
   */
  private Jws<Claims> parseClaims(String token, TokenType tokenType) {
    Key key = (tokenType == TokenType.ACCESS) ? atKey : rtKey;

    return Jwts.parser()
               .verifyWith((SecretKey) key)
               .build()
               .parseSignedClaims(token);
  }

  public String resolveAccessToken(HttpServletRequest request) {
    return resolveTokenFromCookie(request, "access_token");
  }

  public String resolveRefreshToken(HttpServletRequest request) {
    return resolveTokenFromCookie(request, "refresh_token");
  }

  private String resolveTokenFromCookie(
      HttpServletRequest request,
      String cookieName
                                       ) {
    if (request.getCookies() == null) {
      return null;
    }

    return Arrays.stream(request.getCookies())
                 .filter(cookie -> cookieName.equals(cookie.getName()))
                 .map(Cookie::getValue)
                 .findFirst()
                 .orElse(null);
  }

  public long getAccessTokenValidityInMs() {
    return this.accessTokenValidityInMs;
  }

  public long getRefreshTokenValidityInMs() {
    return this.refreshTokenValidityInMs;
  }
}
