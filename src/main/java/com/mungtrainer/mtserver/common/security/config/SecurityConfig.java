package com.mungtrainer.mtserver.common.security.config;

import com.mungtrainer.mtserver.common.security.JwtAuthenticationFilter;
import com.mungtrainer.mtserver.common.security.handler.CustomAccessDeniedHandler;
import com.mungtrainer.mtserver.common.security.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())  // CSRF 비활성화
        // 권한(인가) 예외 처리
        .exceptionHandling(ex -> ex
                               .authenticationEntryPoint(customAuthenticationEntryPoint)  // 401 처리
                               .accessDeniedHandler(customAccessDeniedHandler)            // 403 처리
                          )
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
                                    // 인증 패스
                                   .requestMatchers(HttpMethod.GET, "/api/users/trainer/**").permitAll()

                                    // 인가 검증 절차 받을 api(인증도 함)
                                   .requestMatchers("/api/trainer/**").hasAnyRole("TRAINER","ADMIN")
                                   .requestMatchers(HttpMethod.PATCH,"/api/course/**").hasAnyRole("TRAINER","ADMIN")
                                   .requestMatchers(HttpMethod.DELETE,"/api/course/**").hasAnyRole("TRAINER","ADMIN")

                                   .requestMatchers("/api/users/counseling/**").hasRole("USER")

                                    // User 인증만 받을 api
                                   .requestMatchers("/api/users/**",
                                                    "/api/applications/**",
                                                    "/api/dogs/**",
                                                    "/api/auth/password",
                                                    "/api/auth/logout",
                                                    "/api/presigned-url",
                                                    "/api/auth/check",
                                                    "/api/course/search").authenticated()

                                    // 그 외에는 인증 패스
                                   .anyRequest().permitAll()

                              )
        .formLogin(form -> form.disable())  // 기본 로그인 폼 비활성화
        .httpBasic(basic -> basic.disable()); // Basic 인증 비활성화
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  // 로그인 시 AuthenticationManager 사용
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
