package com.example.demo.global.jwt;

import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.response.ResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends GenericFilterBean {
    private final JwtProvider jwtProvider;
    private final TokenRedisService tokenRedisService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();

        if (requestURI.startsWith("v1/user/signup") ||
                requestURI.startsWith("v1/user/login")
        ) {
            filterChain.doFilter(servletRequest, servletResponse); // 위의 경로들은 필터링을 스킵하고 다음 필터로 이동
            return;
        }

        try {
            // 1. request header에서 jwt 추출
            String token = getTokenFromHeader(httpServletRequest);

            // 2. token validation
            if (token.isBlank() == false && jwtProvider.isValidatedToken(token)) {
                // blacklist check
                if (tokenRedisService.isBlacklisted(token)) {
                    ResponseDto<?> responseDto = ResponseDto.builder()
                            .success(false)
                            .message(ErrorCode.BLACKLISTED_JWT.getMessage())
                            .build();
                    sendErrorResponse((HttpServletResponse) servletResponse, HttpStatus.UNAUTHORIZED, responseDto);
                    return;
                }
                // 유효한 토큰인 경우 authentication 객체를 불러 와서 security context 에 저장
                Authentication authentication = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                ResponseDto<?> responseDto = ResponseDto.builder()
                        .success(false)
                        .message("토큰을 찾을 수 없음")
                        .build();
                sendErrorResponse((HttpServletResponse) servletResponse, HttpStatus.BAD_REQUEST, responseDto);
                return;
            }
        } catch (CustomException e) {
            // custom exception 발생 시 응답 처리
            if (servletResponse.isCommitted() == false) { // 이미 응답이 나갔는지 확인
                ResponseDto<?> responseDto = ResponseDto.builder()
                        .success(false)
                        .message(e.getMessage())
                        .build();
                sendErrorResponse((HttpServletResponse) servletResponse, HttpStatus.valueOf(e.getErrorCode().getHttpStatus()), responseDto);
            }
            return; // 이미 응답이 작성된 경우, 필터 체인을 계속 수행하지 않음
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }


    private String getTokenFromHeader(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token.isBlank() == false && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    // 직접 servlet response 에 메세지 출력
    private void sendErrorResponse(
            HttpServletResponse response, HttpStatus status, ResponseDto<?> responseDto
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        ObjectMapper objectMapper = new ObjectMapper();
        String responseString = objectMapper.writeValueAsString(responseDto);
        response.getWriter().write(responseString);
    }
}
