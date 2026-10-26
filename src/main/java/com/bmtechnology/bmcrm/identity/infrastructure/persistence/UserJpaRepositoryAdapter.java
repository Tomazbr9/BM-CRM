package com.bmtechnology.bmcrm.identity.infrastructure.persistence;

import com.bmtechnology.bmcrm.identity.domain.entity.UserEntity;
import com.bmtechnology.bmcrm.identity.application.port.out.UserRepository;
import com.bmtechnology.bmcrm.identity.domain.valueobjects.Email;
import com.bmtechnology.bmcrm.identity.infrastructure.entity.UserJpaEntity;
import com.bmtechnology.bmcrm.identity.infrastructure.mapper.UserMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserJpaRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    @Override
    public Optional<UserEntity> findByEmail(Email email) {
        return userJpaRepository.findByEmail(email.value()).map(UserMapper::toEntity);
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        return userJpaRepository.findById(id).map(UserMapper::toEntity);
    }

    @Transactional
    @Override
    public UserEntity save(UserEntity usuario) {

        UserJpaEntity userJpa =
                UserMapper.toJpaEntity(usuario);

        entityManager.persist(userJpa);

        return UserMapper.toEntity(userJpa);
    }
}
