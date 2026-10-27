package com.bh.tracker.global.common.exception;

import com.bh.tracker.global.common.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("BaseException: {}", e.getMessage());
        return toResponse(errorCode, ErrorResponse.of(errorCode, e.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
        return toResponse(BaseErrorCode.ENDPOINT_NOT_FOUND, ErrorResponse.of(BaseErrorCode.ENDPOINT_NOT_FOUND));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());
        return toResponse(BaseErrorCode.INVALID_JSON_REQUEST, ErrorResponse.of(BaseErrorCode.INVALID_JSON_REQUEST));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String field = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField())
                .findFirst()
                .orElse(null);
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse(BaseErrorCode.INVALID_REQUEST.getMessage());
        log.warn("ValidationException: {}", message);
        return toResponse(BaseErrorCode.INVALID_REQUEST, ErrorResponse.of(BaseErrorCode.INVALID_REQUEST, message, field));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("MissingServletRequestParameterException: {}", e.getMessage());
        return toResponse(BaseErrorCode.MISSING_PARAMETER, ErrorResponse.of(BaseErrorCode.MISSING_PARAMETER));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        String field = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath().toString())
                .findFirst()
                .orElse(null);
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse(BaseErrorCode.INVALID_REQUEST.getMessage());
        log.warn("ConstraintViolationException: {}", message);
        return toResponse(BaseErrorCode.INVALID_REQUEST, ErrorResponse.of(BaseErrorCode.INVALID_REQUEST, message, field));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        String field = e.getAllValidationResults().stream()
                .map(result -> result.getMethodParameter().getParameterName())
                .findFirst()
                .orElse(null);
        String message = e.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse(BaseErrorCode.INVALID_REQUEST.getMessage());
        log.warn("HandlerMethodValidationException: {}", message);
        return toResponse(BaseErrorCode.INVALID_REQUEST, ErrorResponse.of(BaseErrorCode.INVALID_REQUEST, message, field));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return toResponse(BaseErrorCode.METHOD_NOT_ALLOWED, ErrorResponse.of(BaseErrorCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        return toResponse(BaseErrorCode.UNSUPPORTED_MEDIA_TYPE, ErrorResponse.of(BaseErrorCode.UNSUPPORTED_MEDIA_TYPE));
    }

    // 예: 숫자 path variable에 문자열이 들어온 경우
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("MethodArgumentTypeMismatchException: {}", e.getMessage());
        return toResponse(BaseErrorCode.INVALID_REQUEST,
                ErrorResponse.of(BaseErrorCode.INVALID_REQUEST, BaseErrorCode.INVALID_REQUEST.getMessage(), e.getName()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("UnhandledException: {}", e.getMessage(), e);
        return toResponse(BaseErrorCode.UNHANDLED_ERROR, ErrorResponse.of(BaseErrorCode.UNHANDLED_ERROR));
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode, ErrorResponse body) {
        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }
}
