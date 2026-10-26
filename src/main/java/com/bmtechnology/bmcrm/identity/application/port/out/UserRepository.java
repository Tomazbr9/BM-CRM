package com.bmtechnology.bmcrm.identity.application.port.out;

import com.bmtechnology.bmcrm.identity.domain.entity.UserEntity;
import com.bmtechnology.bmcrm.identity.domain.valueobjects.Email;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<UserEntity> findByEmail(Email email);

    Optional<UserEntity> findById(UUID id);

    UserEntity save(UserEntity user);
}
