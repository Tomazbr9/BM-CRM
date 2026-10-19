package com.tomazbr9.buildprice.budget.domain.exception;

public class BudgetNotEditableException extends RuntimeException {

    public BudgetNotEditableException() {
        super("Budget cannot be modified while completed");
    }
}