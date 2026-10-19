package com.tomazbr9.buildprice.budget.presentation.exception;

import com.tomazbr9.buildprice.budget.application.exception.*;
import com.tomazbr9.buildprice.budget.domain.exception.BudgetNotEditableException;
import com.tomazbr9.buildprice.budget.domain.exception.InvalidBudgetStatusTransitionException;
import com.tomazbr9.buildprice.shared.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class BudgetExceptionHandler {

    @ExceptionHandler(BudgetGroupNotFoundException.class)
    public ResponseEntity<ApiError> handleBudgetGroupNotFound(
            BudgetGroupNotFoundException exception,
            HttpServletRequest request
    ){
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BudgetNotFoundException.class)
    public ResponseEntity<ApiError> handleBudgetNotFound(
            BudgetNotFoundException exception,
            HttpServletRequest request
    ){
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BudgetProjectNotFoundException.class)
    public ResponseEntity<ApiError> handleBudgetProjectNotFound(
            BudgetProjectNotFoundException exception,
            HttpServletRequest request
    ){
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(CompositionNotAvailableForBudgetException.class)
    public ResponseEntity<ApiError> handleCompositionNotAvailableForBudget(
            CompositionNotAvailableForBudgetException exception,
            HttpServletRequest request
    ){
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidParentBudgetGroupException.class)
    public ResponseEntity<ApiError> handleInvalidParentBudgetGroup(
            InvalidParentBudgetGroupException exception,
            HttpServletRequest request
    ){
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(SinapiTableVersionNotFoundException.class)
    public ResponseEntity<ApiError> handleSinapiTableVersionNotFound(
            SinapiTableVersionNotFoundException exception,
            HttpServletRequest request
    ){
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BudgetNotEditableException.class)
    public ResponseEntity<ApiError> handleBudgetNotEditable(
            BudgetNotEditableException exception,
            HttpServletRequest request
    ){
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidBudgetStatusTransitionException.class)
    public ResponseEntity<ApiError> handleInvalidBudgetStatusTransition(
            InvalidBudgetStatusTransitionException exception,
            HttpServletRequest request
    ){
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BudgetItemNotFoundException.class)
    public ResponseEntity<ApiError> handleBudgetItemNotFound(
            BudgetItemNotFoundException exception,
            HttpServletRequest request
    ){
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            String message,
            String path
    ) {

        ApiError error = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }
}
