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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // SPA 구성을 위한 CSRF 핸들러 설정
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();   // 토큰 해석을 Plain Text로 하는 기초 Handler (기본 값: 마스킹 처리된 토큰을 해제 후 비교)
        requestHandler.setCsrfRequestAttributeName(null); // Spring Security 5의 기본 토큰 해석 방식 유지 (React와 가장 협업하기 좋음)

        // 배포 환경(Cross-Site)을 위한 CSRF 쿠키 설정 커스텀
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookieCustomizer(customizer -> customizer
                .sameSite("None")   // Vercel, 서버 도메인 정보가 다르므로, None
                .secure(true) // HTTPS 적용 시 true
        );

        http
                // 1. CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. CSRF 설정
                .csrf(csrf -> csrf
                        // 쿠키에 토큰을 담아 프론트엔드로 전달 (HttpOnly=false로 자바스크립트 접근 허용)
                        .csrfTokenRepository(csrfRepository)
                        .csrfTokenRequestHandler(requestHandler)

                        // Auth, Swagger, Health Check Actuator CSRF 방어 해제
                        .ignoringRequestMatchers(
                                "/auth/**",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/actuator/health"
                                // "/posts", "/posts/**"
                        )
                )
//        .csrf(AbstractHttpConfigurer::disable)
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
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

                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1) // 하나의 계정당 1개의 세션만 허용 (중복 로그인 방지)
                        .maxSessionsPreventsLogin(false) // 새로운 기기에서 로그인하면 기존 기기는 로그아웃됨
                        .expiredSessionStrategy(expiredSessionStrategy()) // 만료된 세션 응답 처리
                )
                .authorizeHttpRequests(auth -> auth
                        // 회원가입/로그인 및 swagger는 허용
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // 회원가입, 로그인, Swagger 제외 요청은 인증/인가 필요
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = parseProperty(allowedOrigins);
        if (!origins.isEmpty()) {
            configuration.setAllowedOrigins(origins);
        }

        List<String> originPatterns = parseProperty(allowedOriginPatterns);
        if (!originPatterns.isEmpty()) {
            configuration.setAllowedOriginPatterns(originPatterns);
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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

    private List<String> parseProperty(String property) {
        if (property == null || property.isBlank()) {
            return List.of();
        }

        return Arrays.stream(property.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }

    private SessionInformationExpiredStrategy expiredSessionStrategy() {
        return event -> writeErrorResponse(
                event.getResponse(),
                HttpStatus.UNAUTHORIZED,
                "다른 기기에서 로그인되어 현재 세션이 만료되었습니다."
        );
    }

    /**
     *
     */
    private static final class CsrfCookieFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                FilterChain filterChain)
                throws ServletException, IOException {

            // 컨텍스트에서 CSRF 토큰 객체를 가져옵니다.
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

            // 토큰의 값을 호출하여 지연된(deferred) 토큰을 강제로 생성하고 쿠키에 담게 만듭니다.
            if (csrfToken != null) {
                csrfToken.getToken();
            }

            filterChain.doFilter(request, response);
        }
    }
}


