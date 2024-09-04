package com.example.demo.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    // jwt
    INVALID_TOKEN(401, "유효하지 않은 토큰"),
    EXPIRED_JWT(401, "만료된 토큰"),
    EMPTY_JWT_CLAIMS(401, "jwt claims 없음"),
    UNSUPPORTED_JWT(401, "지원하지 않는 토큰"),
    BLACKLISTED_JWT(401, "로그아웃된 토큰"),


    BAD_REQUEST(400, "잘못된 요청"),
    FORBIDDEN(403, "접근 권한 없음"), // 유효한 인증은 맞으나, 해당 권한으로 접근 불가,
    RESOURCE_NOT_FOUND(404, "리소스를 찾을 수 없음"),
    DUPLICATE_RESOURCE(409, "이미 사용중인 리소스입니다"),
    INTERNAL_SERVER_ERROR(500, "내부 서버 오류");

    private final int httpStatus;
    private final String message;
}
