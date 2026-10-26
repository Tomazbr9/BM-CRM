package com.bmtechnology.bmcrm.identity.application.exception;

public class ExpiredRefreshTokenException extends RuntimeException {
    public ExpiredRefreshTokenException() {
        super("Refresh Token expirado");
    }
}
