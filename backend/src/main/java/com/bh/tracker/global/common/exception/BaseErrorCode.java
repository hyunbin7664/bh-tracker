package com.bh.tracker.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BaseErrorCode implements ErrorCode {

    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_ERROR_001", "존재하지 않는 경로입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_ERROR_002", "요청값이 올바르지 않습니다."),
    INVALID_JSON_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_ERROR_003", "요청 JSON 형식이 잘못되었습니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON_ERROR_004", "필수 요청 파라미터가 없습니다."),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "COMMON_ERROR_005", "로그인이 필요합니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_ERROR_006", "지원하지 않는 요청 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON_ERROR_007", "지원하지 않는 요청 형식입니다."),
    // 응답 상태는 원래 예외의 4xx 상태를 따름
    UNPROCESSABLE_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_ERROR_008", "처리할 수 없는 요청입니다."),
    UNHANDLED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_ERROR_999", "핸들링하지 않은 서버 오류입니다."),

    // 인증
    // 지점 존재 여부를 노출하지 않도록 실패 사유를 구분하지 않음
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_ERROR_001", "지점 또는 비밀번호가 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
