package com.secondhand.exception;

/**
 * 权限不足（HTTP 403）。
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
