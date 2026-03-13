/**
 * File: null.java
 * Path: com.example.pintbackend.dto.user
 * <p>
 * Outline:
 * custom userdetails wrapper to make @AuthenticationPrincipal return domain user
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.dto.user;


import com.example.pintbackend.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * SecurityFilterChain 이 이걸 써서 유저 정보, authorities, credentials 를 확인함.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final String email;
    private final String username;
    private final String password;
    private final String profileImageS3Key;

    public CustomUserDetails(User user) {
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.profileImageS3Key = user.getProfileImageS3Key();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }


    /**
     * UserDetails 가 interface 라, 인터페이스 에 있는 것들은 다 넣어야 한다.
     * 자바 가 넣으라고 함. 프로젝트에 사용되지는 않는 변수들.
     */
    // admin/user role 구현하면 이 곅체로 저장됨
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
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


}
