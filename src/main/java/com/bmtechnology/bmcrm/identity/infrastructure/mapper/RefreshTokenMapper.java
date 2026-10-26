package com.bmtechnology.bmcrm.identity.infrastructure.mapper;

import com.bmtechnology.bmcrm.identity.domain.entity.RefreshTokenEntity;
import com.bmtechnology.bmcrm.identity.infrastructure.entity.RefreshTokenJpaEntity;
import com.bmtechnology.bmcrm.identity.infrastructure.entity.UserJpaEntity;

public class RefreshTokenMapper {

    private RefreshTokenMapper(){

    }

    public static RefreshTokenJpaEntity toJpaEntity(
            RefreshTokenEntity refreshToken,
            UserJpaEntity user
    ){
        return RefreshTokenJpaEntity.builder()
                .id(refreshToken.getId())
                .user(user)
                .tokenHash(refreshToken.getTokenHash())
                .expiresAt(refreshToken.getExpiresAt())
                .revoked(refreshToken.isRevoked())
                .build();
    }

    public static RefreshTokenEntity toEntity(RefreshTokenJpaEntity refreshToken){
        return RefreshTokenEntity.restore(
                refreshToken.getId(),
                refreshToken.getUser().getId(),
                refreshToken.getTokenHash(),
                refreshToken.getExpiresAt(),
                refreshToken.isRevoked()
        );
    }
}
