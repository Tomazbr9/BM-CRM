package com.tomazbr9.buildprice.budget.application.exception;

public class BudgetItemNotFoundException extends RuntimeException {
    public BudgetItemNotFoundException() {
        super("Orçamento não encontrado");
    }
}
