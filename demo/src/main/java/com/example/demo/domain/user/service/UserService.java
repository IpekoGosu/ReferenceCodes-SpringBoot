package com.example.demo.domain.user.service;

import com.example.demo.domain.user.dto.UserRequestDto;
import com.example.demo.global.response.ResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    ResponseDto<Boolean> checkEmail(String email);

    ResponseDto<?> signup(UserRequestDto userRequestDto);

    ResponseDto<?> login(UserRequestDto userRequestDto, HttpServletResponse response);

    ResponseDto<?> renewAccessToken(HttpServletRequest request);
}
