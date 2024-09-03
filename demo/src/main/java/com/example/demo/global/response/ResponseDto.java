package com.example.demo.global.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@JsonPropertyOrder({"success", "message", "data"})
public class ResponseDto<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ResponseDto<T> success(String message, T data) {
        return new ResponseDto<>(true, message, data);
    }

    public static <T> ResponseDto<T> error(String message) {
        return new ResponseDto<>(false, message, null);
    }

}
