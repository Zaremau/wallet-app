package com.zarema.wallet_app.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ApiError> handleWalletNotFoundException(WalletNotFoundException e) {
        log.warn("Business logic violation: {} - {}", "WALLET_NOT_FOUND", e.getMessage());

        ApiError error = ApiError.builder()
                .message(e.getMessage())
                .code("WALLET_NOT_FOUND")
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ApiError> handleInsufficientFundsException(InsufficientFundsException e) {
        log.warn("Business logic violation: {} - {}", "INSUFFICIENT_FUNDS", e.getMessage());

        ApiError error = ApiError.builder()
                .message(e.getMessage())
                .code("INSUFFICIENT_FUNDS")
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleInvalidJson(HttpMessageNotReadableException ex) {
        log.warn("JSON parsing error: {}", ex.getMessage());

        ApiError error = ApiError.builder()
                .message("Malformed JSON request")
                .code("INVALID_JSON_SYNTAX")
                .build();
        return ResponseEntity.badRequest().body(error);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ApiError error = ApiError.builder()
                .message("Validation failed")
                .code("INVALID_ARGUMENTS")
                .errors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleLockingFailure(PessimisticLockingFailureException e) {
        log.warn("Locking failure: {}", e.getMessage());

        ApiError error = ApiError.builder()
                .message("The wallet is currently busy. Please try again later.")
                .code("RESOURCE_LOCKED")
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllUncaughtExceptions(Exception e) {
        log.error("Internal server error: ", e);

        ApiError error = ApiError.builder()
                .message("An unexpected error occurred")
                .code("INTERNAL_SERVER_ERROR")
                .build();
        return ResponseEntity.internalServerError().body(error);
    }
}