package com.tomazbr9.buildprice.budget.infrastructure.persistence;

import com.tomazbr9.buildprice.budget.application.port.out.ManualItemRepository;
import com.tomazbr9.buildprice.budget.domain.entity.ManualItem;
import com.tomazbr9.buildprice.budget.infrastructure.entity.ManualItemJpaEntity;
import com.tomazbr9.buildprice.budget.infrastructure.mapper.ManualItemMapper;
import com.tomazbr9.buildprice.budget.infrastructure.persistence.ManualItemJpaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ManualItemJpaRepositoryAdapter
        implements ManualItemRepository {

    private final ManualItemJpaRepository repository;
    private final EntityManager entityManager;

    public ManualItemJpaRepositoryAdapter(
            ManualItemJpaRepository repository,
            EntityManager entityManager
    ) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public ManualItem save(ManualItem item) {

        ManualItemJpaEntity entity =
                ManualItemMapper.toJpaEntity(item);

        if (!repository.existsById(item.getId())) {
            entityManager.persist(entity);
            return item;
        }

        return ManualItemMapper.toDomain(
                repository.save(entity)
        );
    }

    @Override
    public Optional<ManualItem> findById(UUID id) {
        return repository
                .findById(id)
                .map(ManualItemMapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}