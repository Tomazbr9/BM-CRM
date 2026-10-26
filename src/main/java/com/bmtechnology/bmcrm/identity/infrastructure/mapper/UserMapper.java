package com.bmtechnology.bmcrm.identity.infrastructure.mapper;

import com.bmtechnology.bmcrm.identity.domain.entity.UserEntity;
import com.bmtechnology.bmcrm.identity.domain.valueobjects.Email;
import com.bmtechnology.bmcrm.identity.infrastructure.entity.UserJpaEntity;

public class UserMapper {

    private UserMapper(){

    }

    public static UserJpaEntity toJpaEntity(UserEntity user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail().value())
                .passwordHash(user.getPasswordHash())
                .role(user.getRole())
                .build();
    }

    public static UserEntity toEntity(UserJpaEntity user) {
        return UserEntity.restore(
                user.getId(),
                user.getName(),
                Email.of(user.getEmail()),
                user.getPasswordHash(),
                user.getRole()
        );
    }
}
