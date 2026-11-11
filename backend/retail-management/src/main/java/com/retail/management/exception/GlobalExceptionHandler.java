
package com.retail.management.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<?> handleDuplicate(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (first, second) -> first
                ));

        return ResponseEntity.badRequest().body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 400,
                        "error", "Validation Failed",
                        "messages", errors,
                        "path", request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(
            InsufficientStockException ex) {

        return buildErrorResponse(
                400,
                "Bad Request",
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOrderState(
            InvalidOrderStateException ex) {

        return buildErrorResponse(
                404,
                "Not found",
                ex.getMessage()
                );
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException ex) {

        return buildErrorResponse(
                409,
                "Conflict",
                "The product was modified by another request. Please refresh and try again."
        );
    }

    @ExceptionHandler(DuplicateCustomerException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateCustomer(
            DuplicateCustomerException ex) {

        return buildErrorResponse(
                409,
                "Conflict",
                ex.getMessage()
        );
    }

    @ExceptionHandler(DuplicateSupplierException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateSupplier(
            DuplicateSupplierException ex) {

        return buildErrorResponse(
                409,
                "Conflict",
                ex.getMessage()
        );
    }

    @ExceptionHandler(InsufficientInventoryException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientInventory(
            InsufficientInventoryException ex
    ) {
        return buildErrorResponse(
                409,
                "Conflict",
                ex.getMessage()
        );
    }


    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleReportNotFound(
            ReportNotFoundException ex
    ) {
        return buildErrorResponse(
                404,
                "Not found",
                ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    private ResponseEntity<?> buildError(
            HttpStatus status,
            String message,
            String path) {

        return ResponseEntity.status(status).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", status.value(),
                        "error", status.getReasonPhrase(),
                        "message", message,
                        "path", path
                )
        );
    }
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            int status,
            String error,
            String message) {

        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(status).body(body);
    }
}
