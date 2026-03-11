package com.example.pintbackend.domain.user.entity;


import com.example.pintbackend.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(name = "username", nullable = false)
  private String userName;

  private String city;  // 거주지

  private String introduction;  // 소개글

  @Column(name = "profile_image_key")
  private String profileImageS3Key;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Builder
  private User(String email, String password, String userName, String city, String introduction,
      String profileImageS3Key, Role role) {
    this.email = email;
    this.password = password;
    this.userName = userName;
    this.city = city;
    this.introduction = introduction;
    this.profileImageS3Key = profileImageS3Key;
    this.role = role;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Role authorityRole = (role == null) ? Role.USER : role;
    return List.of(new SimpleGrantedAuthority("ROLE_" + authorityRole.name()));
  }

  @Override
  public String getUsername() {
    return email; // ID를 이메일로 사용
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public void encodePassword(String encodedPassword) {
    this.password = encodedPassword;
  }

  public void setRole(Role role) {
    this.role = (role == null) ? Role.USER : role;
  }
}
