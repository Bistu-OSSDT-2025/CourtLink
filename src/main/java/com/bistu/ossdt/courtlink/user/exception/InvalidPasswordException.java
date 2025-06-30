package com.bistu.ossdt.courtlink.user.exception;

/**
 * 密码无效异常
 */
public class InvalidPasswordException extends RuntimeException {
    
    public InvalidPasswordException(String message) {
        super(message);
    }
    
    public InvalidPasswordException() {
        super("密码不正确");
    }
} 