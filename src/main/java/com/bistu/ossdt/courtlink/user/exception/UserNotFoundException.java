package com.bistu.ossdt.courtlink.user.exception;

/**
 * 用户不存在异常
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public UserNotFoundException(Long userId) {
        super("用户不存在，ID: " + userId);
    }
    
    public UserNotFoundException(String field, String value) {
        super(String.format("用户不存在，%s: %s", field, value));
    }
} 