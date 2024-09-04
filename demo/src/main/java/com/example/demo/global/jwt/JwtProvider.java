package com.example.demo.global.jwt;

import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.jwt.dto.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

// jwt secret key 생성 : 64바이트 길이의 무작위 문자열 -> base 64 인코딩
// cmd창에서 다음 명령어 입력 (node js)
// node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
// ctUDH62i2w8XOcejGf15W98FTuaPIqayExLrJcFE2Z8

@Component
@Slf4j
public class JwtProvider {
    private final Key key;
    public static long ACCESS_TOKEN_TTL = 2 * 60 * 60 * 1000L; // 2 hours
    public static long REFRESH_TOKEN_TTL = 14 * 24 * 60 * 60 * 1000L; // 2 weeks

    // 상세한 설정(decode 방식)을 하기 위해 직접 생성자 작성
    public JwtProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * create access token from user authorities
     */
    public TokenDto.AccessTokenDto generateAccessToken(Authentication authentication) {
        log.info("Start generating access token, authentication = {}", authentication);

        // 권한 찾기
        String authorities = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.joining(","));
        if (authorities.isEmpty()) {
            authorities = "ROLE_USER"; // 특별한 세팅 없으면 일반 유저
        }

        log.info("권한 가져오기 authorities = {}", authorities);
        log.info("권한 가져오기 authorities getName = {}", authentication.getName());

        Date expirationTime = new Date(System.currentTimeMillis() + ACCESS_TOKEN_TTL);

        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .claim("username", authentication.getName())
                .setExpiration(expirationTime)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new TokenDto.AccessTokenDto("Bearer", accessToken, expirationTime);
    }

    // refresh token은 redis에 저장하게 되므로 Token Redis Service 에서 생성

    /**
     * jwt 복호화
     */
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        log.info("access token claims 복호화 = {}", claims);
        if (claims.get("auth") == null || claims.get("auth").toString().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(claims.get("auth").toString()));

        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // 토큰 정보를 검증
    public boolean isValidatedToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT Token", e);
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT Token", e);
            throw new CustomException(ErrorCode.EXPIRED_JWT);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT Token", e);
            throw new CustomException(ErrorCode.UNSUPPORTED_JWT);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty.", e);
            throw new CustomException(ErrorCode.EMPTY_JWT_CLAIMS);
        }
    }

    public long getExpirationTtl(String accessToken) {
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7).trim();
        }
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody().getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
