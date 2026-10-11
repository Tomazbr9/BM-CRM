package com.tomazbr9.buildprice.budget.infrastructure.persistence;

import com.tomazbr9.buildprice.budget.application.port.out.BudgetItemRepository;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetItem;
import com.tomazbr9.buildprice.budget.infrastructure.entity.BudgetItemJpaEntity;
import com.tomazbr9.buildprice.budget.infrastructure.mapper.BudgetItemMapper;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BudgetItemJpaRepositoryAdapter
        implements BudgetItemRepository {

    private final BudgetItemJpaRepository repository;
    private final EntityManager entityManager;

    public BudgetItemJpaRepositoryAdapter(
            BudgetItemJpaRepository repository,
            EntityManager entityManager
    ) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public BudgetItem save(BudgetItem item) {

        BudgetItemJpaEntity entity =
                BudgetItemMapper.toJpaEntity(item);

        if (!repository.existsById(item.getId())) {
            entityManager.persist(entity);
            return item;
        }

        return BudgetItemMapper.toDomain(
                repository.save(entity)
        );
    }

    @Override
    public Optional<BudgetItem> findById(UUID id) {
        return repository
                .findById(id)
                .map(BudgetItemMapper::toDomain);
    }

    @Override
    public List<BudgetItem> findByBudgetGroupId(
            UUID budgetGroupId
    ) {
        return repository
                .findByBudgetGroupId(
                        budgetGroupId
                )
                .stream()
                .map(BudgetItemMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<BudgetItem> findByBudgetGroupIds(
            Collection<UUID> budgetGroupIds
    ) {

        if (budgetGroupIds == null || budgetGroupIds.isEmpty()) {
            return List.of();
        }

        return repository
                .findByBudgetGroupIdIn(
                        budgetGroupIds
                )
                .stream()
                .map(BudgetItemMapper::toDomain)
                .toList();
    }
}