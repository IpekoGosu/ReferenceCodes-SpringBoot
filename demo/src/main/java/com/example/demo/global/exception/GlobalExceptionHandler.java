package com.example.demo.global.exception;

import com.example.demo.global.response.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ResponseDto<?>> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ResponseDto<?>> handleException(Exception e) {
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ResponseDto.error(e.getMessage()));
    }
}
