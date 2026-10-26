package com.bmtechnology.bmcrm.identity.application.port.out;

public interface PasswordHasher {
    String hash(String password);
}