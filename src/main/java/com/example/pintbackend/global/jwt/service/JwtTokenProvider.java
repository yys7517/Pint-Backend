package com.example.pintbackend.global.jwt.service;

import com.example.pintbackend.global.jwt.dto.JwtTokenInfo;
import com.example.pintbackend.global.jwt.exception.JwtAuthenticationException;
import com.example.pintbackend.global.jwt.exception.TokenHasExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  private final Key key;
  private final long accessTokenExpireMs;
  private final long refreshTokenExpireMs;

  // application.yml에서 secret 및 토큰 만료 시간 값 가져오기
  public JwtTokenProvider(
      @Value("${jwt.secret-key}") String secretKey,
      @Value("${jwt.access-token-expire-ms}") long accessTokenExpireMs,
      @Value("${jwt.refresh-token-expire-ms}") long refreshTokenExpireMs
  ) {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
    this.accessTokenExpireMs = accessTokenExpireMs;
    this.refreshTokenExpireMs = refreshTokenExpireMs;
  }

  /**
   * Authentication 객체를 기반으로, access, refresh 토큰을 생성하는 메서드
   * @param authentication
   * @return JwtTokenInfo
   */
  public JwtTokenInfo generateToken(Authentication authentication) {

    // 인증(Authentication) 객체를 사용하여 권한 정보 가져오기
    String authorities = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));

    long now = (new Date()).getTime();

    // Access Token 생성
    Date accessTokenExpiresIn = new Date(now + accessTokenExpireMs);
    String accessToken = Jwts.builder()
        .setSubject(authentication.getName())
        .claim("auth", authorities)   // auth Claim에 권한 설정
        .setExpiration(accessTokenExpiresIn)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();

    // Refresh Token 생성
    String refreshToken = Jwts.builder()
        .setExpiration(new Date(now + refreshTokenExpireMs))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();

    return JwtTokenInfo.builder()
        .grantType("Bearer")
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  /**
   * Jwt 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
   */
  public Authentication getAuthentication(String accessToken) {
    // Jwt 토큰 복호화하여 토큰 내 정보 확인
    Claims claims = parseClaims(accessToken);

    if (claims.get("auth") == null) {
      throw new JwtAuthenticationException("권한 정보가 없는 토큰입니다.");
    }

    // auth 클레임에서 권한 정보 가져오기
    Collection<? extends GrantedAuthority> authorities = Arrays.stream(
            claims.get("auth").toString().split(","))
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());

    // UserDetails 객체를 만들어서 Authentication return
    // UserDetails: interface, User: UserDetails를 구현한 class
    UserDetails principal = new User(claims.getSubject(), "", authorities);
    return new UsernamePasswordAuthenticationToken(principal, "", authorities);
  }

  // 토큰 정보를 검증하는 메서드
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      log.info("Invalid JWT Token", e);
      throw new JwtAuthenticationException("유효하지 않은 토큰입니다.");
    } catch (ExpiredJwtException e) {
      log.info("Expired JWT Token", e);
      throw new TokenHasExpiredException("토큰이 만료되었습니다.");
    } catch (UnsupportedJwtException e) {
      log.info("Unsupported JWT Token", e);
      throw new JwtAuthenticationException("지원하지 않는 토큰입니다.");
    } catch (IllegalArgumentException e) {
      log.info("JWT claims string is empty.", e);
      throw new JwtAuthenticationException("토큰 정보가 비어있습니다.");
    }
  }


  // accessToken
  private Claims parseClaims(String accessToken) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(accessToken)
          .getBody();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }

}
