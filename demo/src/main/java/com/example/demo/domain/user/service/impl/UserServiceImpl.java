package com.example.demo.domain.user.service.impl;

import com.example.demo.domain.user.dto.UserRequestDto;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.entity.UserRole;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.jwt.TokenRedisService;
import com.example.demo.global.jwt.dto.TokenDto;
import com.example.demo.global.redis.RedisService;
import com.example.demo.global.response.ResponseDto;
import com.example.demo.global.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtProvider jwtProvider;
    private final TokenRedisService tokenRedisService;
    private final RedisService redisService;

    @Override
    public ResponseDto<Boolean> checkEmail(String email) {
        Optional<User> optionalUser = userRepository.findOptionalByEmail(email);
        if (optionalUser.isPresent()) {
            return ResponseDto.success("중복 이메일", false);
        }
        return ResponseDto.success("사용 가능한 이메일", true);
    }

    @Override
    @Transactional
    public ResponseDto<?> signup(UserRequestDto userRequestDto) {
        log.info("회원가입 요청");

        if (checkEmail(userRequestDto.email()).getData() == false) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "중복된 이메일");
        }
        if (isValidPassword(userRequestDto.password()) == false) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "잘못된 비밀번호");
        }

        userRepository.save(User.builder()
                .email(userRequestDto.email())
                .password(passwordEncoder.encode(userRequestDto.password()))
                .role(UserRole.USER)
                .build());

        return ResponseDto.success("회원가입 성공", true);
    }

    @Override
    public ResponseDto<?> login(UserRequestDto userRequestDto, HttpServletResponse response) {
        log.info("로그인 요청");
        // 비밀번호 검증
        User user = userRepository.findByEmail(userRequestDto.email());
        if (passwordEncoder.matches(userRequestDto.password(), user.getPassword()) == false) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "잘못된 비밀번호");
        }
        // Authentication 생성, 인증 정보 가져오기
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userRequestDto.email(), userRequestDto.password());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        log.info("authentication = {}", authentication);
        // jwt 생성
        TokenDto.AccessTokenDto accessTokenDto = jwtProvider.generateAccessToken(authentication);
        TokenDto.RefreshTokenDto refreshTokenDto = tokenRedisService.generateRefreshToken(authentication, userRequestDto.email());
        TokenDto tokenDto = new TokenDto(accessTokenDto, refreshTokenDto);
        // cookie에 refresh token값 담아주기
        CookieUtil.addCookie(response, "refreshToken", refreshTokenDto.token(), JwtProvider.REFRESH_TOKEN_TTL / 1000L);

        log.info("{}가 로그인했습니다", userRequestDto.email());

        // access token은 프론트가 클라이언트의 local storage 등에 보관
        return ResponseDto.success("로그인 성공", tokenDto);
    }

    @Override
    public ResponseDto<?> renewAccessToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("renew access token");
        // cookie에 있는 refresh token 찾기
        String refreshToken = CookieUtil.getRefreshTokenCookie(request);
        // cookie에서 refresh token을 찾아서, email 가져오기
        String cookieEmail = jwtProvider.parseClaims(refreshToken).get("username").toString();
        log.info("cookie email = {}", cookieEmail);
        // redis에 저장된 email 찾기
        String redisEmail = tokenRedisService.findEmailInRedis(refreshToken);
        log.info("redis email = {}", redisEmail);
        // 비교 검증
        if (cookieEmail.equals(redisEmail) == false) {
            throw new CustomException(ErrorCode.INVALID_TOKEN, "invalid refresh token");
        }
        // create new access token
        Authentication authentication = new UsernamePasswordAuthenticationToken(redisEmail, null);
        return ResponseDto.success("access token 재발급 성공", jwtProvider.generateAccessToken(authentication));
    }

    @Override
    public ResponseDto<?> logout()


    private boolean isValidPassword(String password) {
        // 8자 이상, 특수문자 포함
        return password.length() >= 8 && password.matches(".*[!@#$%^&*()-+=].*");
    }

}
