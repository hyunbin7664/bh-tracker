package com.bh.tracker.global.common.exception;

import com.bh.tracker.global.common.response.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
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
        FieldError error = e.getBindingResult().getFieldError();
        String field = error != null ? error.getField() : null;
        String message = error != null ? error.getDefaultMessage() : BaseErrorCode.INVALID_REQUEST.getMessage();
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
        ConstraintViolation<?> violation = e.getConstraintViolations().stream().findFirst().orElse(null);
        String field = violation != null ? violation.getPropertyPath().toString() : null;
        String message = violation != null ? violation.getMessage() : BaseErrorCode.INVALID_REQUEST.getMessage();
        log.warn("ConstraintViolationException: {}", message);
        return toResponse(BaseErrorCode.INVALID_REQUEST, ErrorResponse.of(BaseErrorCode.INVALID_REQUEST, message, field));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        // field와 message를 같은 검증 결과에서 꺼내야 서로 다른 파라미터의 이름·메시지가 섞이지 않음
        ParameterValidationResult result = e.getAllValidationResults().stream().findFirst().orElse(null);
        String field = result != null ? result.getMethodParameter().getParameterName() : null;
        String message = result != null && !result.getResolvableErrors().isEmpty()
                ? result.getResolvableErrors().get(0).getDefaultMessage()
                : BaseErrorCode.INVALID_REQUEST.getMessage();
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
        // 개별 핸들러가 없는 스프링 MVC 예외(406 등)도 원래 4xx 상태를 유지해 클라이언트 오류와 서버 오류를 구분
        if (e instanceof org.springframework.web.ErrorResponse errorResponse
                && errorResponse.getStatusCode().is4xxClientError()) {
            log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage());
            return ResponseEntity.status(errorResponse.getStatusCode())
                    .body(ErrorResponse.of(BaseErrorCode.UNPROCESSABLE_REQUEST));
        }
        log.error("UnhandledException: {}", e.getMessage(), e);
        return toResponse(BaseErrorCode.UNHANDLED_ERROR, ErrorResponse.of(BaseErrorCode.UNHANDLED_ERROR));
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode, ErrorResponse body) {
        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }
}
