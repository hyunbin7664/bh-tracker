package com.bh.tracker.domain.auth.exception;

import com.bh.tracker.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    // 지점 존재 여부를 노출하지 않도록 지점 미존재와 비밀번호 불일치를 같은 코드로 응답
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_ERROR_001", "지점 또는 비밀번호가 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
