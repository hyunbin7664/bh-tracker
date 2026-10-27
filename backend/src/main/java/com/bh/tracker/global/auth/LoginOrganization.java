package com.bh.tracker.global.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 로그인 세션의 지점 id(Long)를 컨트롤러 파라미터로 주입받는다.
 * 지점 스코핑이 필요한 API는 요청 본문 대신 이 값을 기준으로 동작한다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginOrganization {
}
