package com.tomazbr9.buildprice.budget.infrastructure.persistence;

import com.tomazbr9.buildprice.budget.infrastructure.entity.BudgetItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BudgetItemJpaRepository
        extends JpaRepository<BudgetItemJpaEntity, UUID> {

    List<BudgetItemJpaEntity> findByBudgetGroupId(
            UUID budgetGroupId
    );
}