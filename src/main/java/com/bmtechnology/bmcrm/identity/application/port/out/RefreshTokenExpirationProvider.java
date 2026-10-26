package com.bmtechnology.bmcrm.identity.application.port.out;

import java.time.LocalDateTime;

public interface RefreshTokenExpirationProvider {
    LocalDateTime expiresAt();
}
