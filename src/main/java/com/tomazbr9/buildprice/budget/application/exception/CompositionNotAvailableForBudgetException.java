package com.tomazbr9.buildprice.budget.application.exception;

public class CompositionNotAvailableForBudgetException extends RuntimeException {
    public CompositionNotAvailableForBudgetException() {
        super("A composição não está disponível para este orçamento.");
    }
}
