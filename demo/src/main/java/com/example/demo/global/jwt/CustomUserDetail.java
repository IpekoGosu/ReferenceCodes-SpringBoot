package com.example.demo.global.jwt;

import com.example.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
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
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findOptionalByEmail(email)
                .map(user -> createUserDetails(user))
                .orElseThrow(() -> new UsernameNotFoundException("cannot find user"));
    }

    private UserDetails createUserDetails(com.example.demo.domain.user.entity.User user) {
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
