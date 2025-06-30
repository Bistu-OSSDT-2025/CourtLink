package com.bistu.ossdt.courtlink.user.exception;

/**
 * 用户已存在异常
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    public UserAlreadyExistsException(String field, String value) {
        super(String.format("%s已存在: %s", field, value));
    }
} 