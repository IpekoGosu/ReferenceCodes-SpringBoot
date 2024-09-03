package com.example.demo.global.jwt.dto;

import java.util.Date;

/**
 * json web token information dto
 * @param accessTokenDto
 * @param refreshTokenDto
 */
public record TokenDto(
    AccessTokenDto accessTokenDto, RefreshTokenDto refreshTokenDto
) {
    public record RefreshTokenDto(String token, Date expiredTime) {}

    public record AccessTokenDto(String grantType, String token, Date expiredTime) {}
}
