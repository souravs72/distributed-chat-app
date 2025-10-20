package com.chatapp.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private LocalDateTime timestamp;
    private String traceId;

    public static <T> BaseResponse<T> success(T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.success = true;
        response.data = data;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    public static <T> BaseResponse<T> success(T data, String message) {
        BaseResponse<T> response = new BaseResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    public static <T> BaseResponse<T> error(String message) {
        BaseResponse<T> response = new BaseResponse<>();
        response.success = false;
        response.message = message;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    public static <T> BaseResponse<T> error(String message, String errorCode) {
        BaseResponse<T> response = new BaseResponse<>();
        response.success = false;
        response.message = message;
        response.errorCode = errorCode;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    // Manual getter for success field to ensure compatibility
    public boolean isSuccess() {
        return success;
    }
}
