package com.bmtechnology.bmcrm.identity.infrastructure.security;

import com.bmtechnology.bmcrm.identity.application.port.out.RefreshTokenGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SecureRefreshTokenGenerator implements RefreshTokenGenerator {

    private static final int TOKEN_SIZE_BYTS = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {

        byte[] bytes = new byte[TOKEN_SIZE_BYTS];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
