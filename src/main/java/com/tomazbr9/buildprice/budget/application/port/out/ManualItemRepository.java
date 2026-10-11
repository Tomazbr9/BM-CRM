package com.tomazbr9.buildprice.budget.application.port.out;

import com.tomazbr9.buildprice.budget.domain.entity.ManualItem;

import java.util.Optional;
import java.util.UUID;

public interface ManualItemRepository {

    ManualItem save(ManualItem item);

    Optional<ManualItem> findById(UUID id);

    void deleteById(UUID id);
}