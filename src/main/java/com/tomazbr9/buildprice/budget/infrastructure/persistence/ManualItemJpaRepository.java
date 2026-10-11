package com.tomazbr9.buildprice.budget.infrastructure.persistence;

import com.tomazbr9.buildprice.budget.infrastructure.entity.ManualItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ManualItemJpaRepository
        extends JpaRepository<ManualItemJpaEntity, UUID> {
}