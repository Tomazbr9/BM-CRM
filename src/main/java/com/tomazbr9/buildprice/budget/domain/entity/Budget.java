package com.tomazbr9.buildprice.budget.domain.entity;

import com.tomazbr9.buildprice.budget.domain.exception.BudgetNotEditableException;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetStatus;
import com.tomazbr9.buildprice.budget.domain.exception.InvalidBudgetStatusTransitionException;

import java.math.BigDecimal;
import java.util.UUID;

public class Budget {

    private final UUID id;
    private final UUID projectId;
    private final UUID sinapiTableVersionId;

    private BudgetStatus status;
    private BigDecimal bdiPercentage;

    private Budget(
            UUID id,
            UUID projectId,
            UUID sinapiTableVersionId,
            BudgetStatus status,
            BigDecimal bdiPercentage
    ) {
        this.id = validateId(id);
        this.projectId = validateProjectId(projectId);
        this.sinapiTableVersionId = validateSinapiTableVersionId(sinapiTableVersionId);
        this.status = validateStatus(status);
        this.bdiPercentage = validateBdiPercentage(bdiPercentage);
    }

    public static Budget create(
            UUID projectId,
            UUID sinapiTableVersionId,
            BigDecimal defaultBdiPercentage
    ) {
        return new Budget(
                UUID.randomUUID(),
                projectId,
                sinapiTableVersionId,
                BudgetStatus.DRAFT,
                defaultBdiPercentage
        );
    }

    public static Budget restore(
            UUID id,
            UUID projectId,
            UUID sinapiTableVersionId,
            BudgetStatus status,
            BigDecimal bdiPercentage
    ) {
        return new Budget(
                id,
                projectId,
                sinapiTableVersionId,
                status,
                bdiPercentage
        );
    }

    public void updateBdiPercentage(
            BigDecimal bdiPercentage
    ) {

        if (bdiPercentage == null) {
            throw new IllegalArgumentException(
                    "BDI percentage is required"
            );
        }

        if (bdiPercentage.signum() < 0) {
            throw new IllegalArgumentException(
                    "BDI percentage cannot be negative"
            );
        }

        this.bdiPercentage =
                validateBdiPercentage(
                        bdiPercentage
                );
    }

    public void complete() {
        if (status != BudgetStatus.DRAFT) {
            throw new InvalidBudgetStatusTransitionException(
                    "Only draft budgets can be completed"
            );
        }

        this.status = BudgetStatus.COMPLETED;
    }

    public void reopen() {
        if (status != BudgetStatus.COMPLETED) {
            throw new InvalidBudgetStatusTransitionException(
                    "Only completed budgets can be reopened"
            );
        }

        this.status = BudgetStatus.DRAFT;
    }

    public void ensureEditable() {
        if (status != BudgetStatus.DRAFT) {
            throw new BudgetNotEditableException();
        }
    }

    private static UUID validateId(UUID id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "ID do orçamento é obrigatório"
            );
        }

        return id;
    }

    private static UUID validateProjectId(
            UUID projectId
    ) {

        if (projectId == null) {
            throw new IllegalArgumentException(
                    "Projeto é obrigatório"
            );
        }

        return projectId;
    }

    private static UUID validateSinapiTableVersionId(
            UUID sinapiTableVersionId
    ) {

        if (sinapiTableVersionId == null) {
            throw new IllegalArgumentException(
                    "Tabela de versão SINAPI é obrigatório"
            );
        }

        return sinapiTableVersionId;
    }

    private static BudgetStatus validateStatus(
            BudgetStatus status
    ) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Status do BDI é obrigatorio"
            );
        }

        return status;
    }

    private static BigDecimal validateBdiPercentage(
            BigDecimal bdiPercentage
    ) {

        if (bdiPercentage == null) {
            return BigDecimal.ZERO;
        }

        if (bdiPercentage.signum() < 0) {
            throw new IllegalArgumentException(
                    "Percentual do BDI não pode ser negativo"
            );
        }

        return bdiPercentage;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public UUID getSinapiTableVersionId() {
        return sinapiTableVersionId;
    }

    public BudgetStatus getStatus() {
        return status;
    }

    public BigDecimal getBdiPercentage() {
        return bdiPercentage;
    }
}