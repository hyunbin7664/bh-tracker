package com.bh.tracker.global.auth;

import com.bh.tracker.global.common.exception.BaseErrorCode;
import com.bh.tracker.global.common.exception.BaseException;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class LoginOrganizationArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (!parameter.hasParameterAnnotation(LoginOrganization.class)) {
            return false;
        }
        // 넘기면 기본 리졸버가 요청 파라미터로 바인딩해 지점 id 조작이 가능해짐
        if (!Long.class.equals(parameter.getParameterType())) {
            throw new IllegalStateException("@LoginOrganization은 Long 타입 파라미터에만 사용할 수 있습니다: " + parameter);
        }
        return true;
    }

    @Override
    public Long resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long organizationId)) {
            throw new BaseException(BaseErrorCode.UNAUTHENTICATED);
        }
        return organizationId;
    }
}
