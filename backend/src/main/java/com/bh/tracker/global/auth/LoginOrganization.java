package com.bh.tracker.global.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 로그인 세션의 지점 id(Long)를 컨트롤러 파라미터로 주입받는다.
 * 지점 스코핑이 필요한 API는 요청 본문 대신 이 값을 기준으로 동작한다.
 * (요청에서 지점 id를 받으면 다른 지점 id로 조작할 수 있어서, 조작할 수 없는 세션 값을 쓴다)
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginOrganization {
}
