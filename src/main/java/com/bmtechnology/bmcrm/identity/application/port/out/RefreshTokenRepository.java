package com.bmtechnology.bmcrm.identity.application.port.out;

import com.bmtechnology.bmcrm.identity.domain.entity.RefreshTokenEntity;

import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshTokenEntity save(RefreshTokenEntity refreshToken);

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
}
