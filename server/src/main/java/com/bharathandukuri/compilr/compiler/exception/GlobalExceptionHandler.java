package com.bharathandukuri.compilr.compiler.exception;

import com.bharathandukuri.compilr.execution.exception.DockerException;
import com.bharathandukuri.compilr.execution.exception.IsolateException;
import com.bharathandukuri.compilr.language.exception.LanguageNotFoundException;
import com.bharathandukuri.compilr.protection.queue.CapacityUnavailableException;
import com.bharathandukuri.compilr.protection.queue.ExecutionQueueTimeoutException;
import com.bharathandukuri.compilr.protection.ratelimit.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCompilerRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            InvalidCompilerRequestException ex,
            HttpServletRequest request
    ) {
        log.warn("Invalid compiler request on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UnsupportedLanguageException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedLanguage(
            UnsupportedLanguageException ex,
            HttpServletRequest request
    ) {
        log.warn("Unsupported language on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(LanguageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLanguageNotFound(
            LanguageNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Language not found on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(OutputLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleOutputLimitExceeded(
            OutputLimitExceededException ex,
            HttpServletRequest request
    ) {
        log.warn("Output limit exceeded on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException ex,
            HttpServletRequest request
    ) {
        log.warn("Rate limit exceeded on [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(errorResponse);
    }

    @ExceptionHandler(CapacityUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleCapacityUnavailable(
            CapacityUnavailableException ex,
            HttpServletRequest request
    ) {
        log.warn("Capacity unavailable on [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "5")
                .body(errorResponse);
    }

    @ExceptionHandler(ExecutionQueueTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleQueueTimeout(
            ExecutionQueueTimeoutException ex,
            HttpServletRequest request
    ) {
        log.warn("Queue timeout on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.GATEWAY_TIMEOUT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ExecutionTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleExecutionTimeout(
            ExecutionTimeoutException ex,
            HttpServletRequest request
    ) {
        log.warn("Execution timeout on [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.GATEWAY_TIMEOUT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DockerUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleDockerUnavailable(
            DockerUnavailableException ex,
            HttpServletRequest request
    ) {
        log.error("Docker daemon unavailable on [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Execution engine is currently unavailable. Please try again shortly.", request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        if (details.isBlank()) {
            details = "Validation failed for execution request.";
        }
        log.warn("Validation error on [{}]: {}", request.getRequestURI(), details);
        return buildResponse(HttpStatus.BAD_REQUEST, details, request.getRequestURI());
    }

    @ExceptionHandler({DockerException.class, IsolateException.class})
    public ResponseEntity<ErrorResponse> handleEngineException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Sandbox engine exception on [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Execution engine encountered an internal fault. Please verify code or retry.", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled server exception on [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred.", request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, String path) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
        return ResponseEntity.status(status).body(errorResponse);
    }
}
