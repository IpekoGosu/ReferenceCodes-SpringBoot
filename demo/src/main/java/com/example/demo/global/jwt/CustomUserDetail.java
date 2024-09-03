package com.example.demo.global.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * Spring Security의 UserDetails 인터페이스의 사용자 정보를 매핑하는 클래스
 * 사용자 인증 및 권한 관리
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetail implements UserDetailsService {


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
