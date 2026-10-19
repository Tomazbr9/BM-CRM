package com.tomazbr9.buildprice.budget.domain.exception;

public class InvalidBudgetStatusTransitionException
        extends RuntimeException {

    public InvalidBudgetStatusTransitionException(String message) {
        super(message);
    }
}