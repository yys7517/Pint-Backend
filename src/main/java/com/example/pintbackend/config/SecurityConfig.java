package com.example.pintbackend.config;

import com.example.pintbackend.dto.common.response.BaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final ObjectMapper objectMapper;

  @Value("${cors.allowed-origins}")
  private String allowedOrigins;

  @Value("${cors.allowed-origin-patterns}")
  private String allowedOriginPatterns;

  @Value("${server.servlet.session.cookie.secure:true}")
  private boolean secureCookie;

  @Value("${server.servlet.session.cookie.same-site:None}")
  private String sameSite;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()
//            .csrfTokenRepository(csrfTokenRepository())
//            .ignoringRequestMatchers(
//                "/auth/login", "/auth/signup", "/auth/signout", "/auth/unique",
//                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
//                "/actuator/health"
////                "/posts", "/posts/**"
//                )
        )
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .httpBasic(AbstractHttpConfigurer::disable) // 기본 인증 로그인 비활성화
        .formLogin(AbstractHttpConfigurer::disable) // 기본 login form 비활성화
        .logout(AbstractHttpConfigurer::disable) // 기본 logout 비활성화
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint((request, response, authException) ->
                writeErrorResponse(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
                )
            )
            .accessDeniedHandler((request, response, accessDeniedException) ->
                writeErrorResponse(
                    response,
                    HttpStatus.FORBIDDEN,
                    "접근 권한이 없습니다."
                )
            )
        )
        .headers(c -> c.frameOptions(FrameOptionsConfig::disable).disable()) // X-Frame-Options 비활성화
        .sessionManagement(sm -> sm
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(1) // 하나의 계정당 1개의 세션만 허용 (중복 로그인 방지)
            .maxSessionsPreventsLogin(false) // 새로운 기기에서 로그인하면 기존 기기는 로그아웃됨
        )
        .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
        .authorizeHttpRequests(auth -> auth
                // 회원가입/로그인 및 swagger는 허용
                .requestMatchers("/auth/login", "/auth/signup", "/auth/signout", "/auth/unique").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
//                .requestMatchers( "/posts" ,"/posts/**").permitAll()
//                .requestMatchers("/posts/**").authenticated()
                .anyRequest().authenticated()
        );

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public CookieCsrfTokenRepository csrfTokenRepository() {
    CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    repository.setCookieCustomizer(cookie -> cookie
        .path("/")
        .secure(secureCookie)
        .sameSite(sameSite)
    );
    return repository;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(parseProperty(allowedOrigins));
    configuration.setAllowedOriginPatterns(parseProperty(allowedOriginPatterns));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  private List<String> parseProperty(String value) {
    return Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(entry -> !entry.isEmpty())
        .toList();
  }

  private void writeErrorResponse(
      HttpServletResponse response,
      HttpStatus status,
      String message
  ) throws java.io.IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(
        objectMapper.writeValueAsString(BaseResponse.fail(status.value(), message))
    );
  }

  /**
   *
   */
  private static final class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
      CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
      if (csrfToken != null) {
        csrfToken.getToken();
      }
      filterChain.doFilter(request, response);
    }
  }

}
