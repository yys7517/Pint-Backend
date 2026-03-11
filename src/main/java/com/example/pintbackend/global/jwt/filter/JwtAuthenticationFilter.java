package com.example.pintbackend.global.jwt.filter;

import com.example.pintbackend.dto.common.response.BaseResponse;
import com.example.pintbackend.global.jwt.exception.JwtAuthenticationException;
import com.example.pintbackend.global.jwt.exception.TokenHasExpiredException;
import com.example.pintbackend.global.jwt.service.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 1. Request Header에서 Access 토큰 추출
        String token = resolveToken(request);

        // 2. validateToken으로 토큰 유효성 검사
        if (token != null) {
            try {
                jwtTokenProvider.validateToken(token);

                // 토큰이 유효할 경우 토큰에서 Authentication 객체를 가지고 와서 SecurityContext에 저장
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (TokenHasExpiredException e) {
                throwUnauthorizedResponse(response, "토큰이 만료되었습니다.");
                return;
            } catch (JwtAuthenticationException e) {
                throwUnauthorizedResponse(response, e.getMessage());
                return;
            }
        }
        chain.doFilter(request, response);
    }

  /**
   *  Request Header에서 Bearer 값을 제외한 Access Token 받기
   */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");  // Header에서 Authorization 값 가져오기
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void throwUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
            new ObjectMapper().writeValueAsString(BaseResponse.of(401, message, ""))
        );
    }
}
