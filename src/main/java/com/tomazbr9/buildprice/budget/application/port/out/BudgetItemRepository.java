package com.tomazbr9.buildprice.budget.application.port.out;

import com.tomazbr9.buildprice.budget.domain.entity.BudgetItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetItemRepository {

    BudgetItem save(BudgetItem item);

    Optional<BudgetItem> findById(UUID id);

    List<BudgetItem> findByBudgetGroupId(
            UUID budgetGroupId
    );

    void deleteById(UUID id);
}