package com.tomazbr9.buildprice.budget.application.exception;

public class BudgetGroupNotFoundException extends RuntimeException {
    public BudgetGroupNotFoundException() {
        super("Grupo de orçamento não encontrado");
    }
}
