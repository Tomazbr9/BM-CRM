package com.bmtechnology.bmcrm.identity.application.exception;

public class RevokedRefreshTokenException extends RuntimeException {
    public RevokedRefreshTokenException() {
        super("Refresh Token revogado");
    }
}
