package com.example.demo.global.jwt;

import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.jwt.dto.TokenDto;
import com.example.demo.global.redis.RedisService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class TokenRedisService {
    private final RedisService redisService;
    private final JwtProvider jwtProvider;
    private final Key key;

    private static final String REFRESH_TOKEN_PREFIX = "refreshToken:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public TokenRedisService(
            RedisService redisService, JwtProvider jwtProvider, @Value("${jwt.secret}") String secretKey
    ) {
        this.redisService = redisService;
        this.jwtProvider = jwtProvider;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }


    @Transactional
    public TokenDto.RefreshTokenDto generateRefreshToken(Authentication authentication, String userEmail) {
        String authorities = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.joining(","));
        if (authorities.isEmpty()) {
            authorities = "ROLE_USER"; // 특별한 세팅 없으면 일반 유저
        }
        Date expirationTime = new Date(System.currentTimeMillis() + JwtProvider.REFRESH_TOKEN_TTL);

        String refreshToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .claim("username", authentication.getName())
                .setExpiration(expirationTime)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // set redis entry
        redisService.set(REFRESH_TOKEN_PREFIX + refreshToken, userEmail, JwtProvider.REFRESH_TOKEN_TTL);

        return new TokenDto.RefreshTokenDto(refreshToken, expirationTime);
    }

    /**
     * @param refreshToken refresh token
     * @return user email
     */
    public String findEmailInRedis(String refreshToken) {
        String email = redisService.get(REFRESH_TOKEN_PREFIX + refreshToken);
        if (email.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return email;
    }

    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        redisService.delete(REFRESH_TOKEN_PREFIX + refreshToken);
    }

    /**
     * access token을 남은 ttl동안 블랙리스트에 추가하여 로그인 차단
     */
    @Transactional
    public void addToBlacklist(String bearerToken) {
        String accessToken = getToken(bearerToken);
        long expiration = jwtProvider.getExpirationTtl(accessToken);
        redisService.set(BLACKLIST_PREFIX + accessToken, accessToken, expiration);
    }

    public boolean isBlacklisted(String accessToken) {
        return redisService.hasKey(BLACKLIST_PREFIX + accessToken);
    }

    private String getToken(String bearerToken) {
        if (bearerToken.isBlank() == false && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }
}
