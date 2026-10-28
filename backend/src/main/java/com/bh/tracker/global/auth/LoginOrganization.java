package com.bh.tracker.global.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 로그인 세션의 지점 id(Long)를 주입한다.
 * 지점 id를 요청으로 받으면 조작될 수 있으므로, 지점 스코핑은 항상 이 값을 기준으로 한다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginOrganization {
}
