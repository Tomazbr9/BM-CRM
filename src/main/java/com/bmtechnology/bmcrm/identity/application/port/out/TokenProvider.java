package com.bmtechnology.bmcrm.identity.application.port.out;

public interface TokenProvider {
    String generateAccessToken(String email, String role);
}
