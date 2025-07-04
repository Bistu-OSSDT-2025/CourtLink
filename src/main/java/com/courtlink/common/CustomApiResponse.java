package com.courtlink.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> CustomApiResponse<T> success(T data) {
        return new CustomApiResponse<>(true, "Success", data);
    }

    public static <T> CustomApiResponse<T> success(String message, T data) {
        return new CustomApiResponse<>(true, message, data);
    }

    public static <T> CustomApiResponse<T> error(String message) {
        return new CustomApiResponse<>(false, message, null);
    }

    public static <T> CustomApiResponse<T> error(String message, T data) {
        return new CustomApiResponse<>(false, message, data);
    }
} 
