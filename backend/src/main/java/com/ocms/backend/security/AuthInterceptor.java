package com.ocms.backend.security;

import com.ocms.backend.common.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;

    public AuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BizException(401, "请先登录");
        }
        LoginUser loginUser = jwtService.parse(token.substring(7));
        AuthContext.set(loginUser);

        RequireRole roleAnn = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (roleAnn == null) {
            roleAnn = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (roleAnn != null && Arrays.stream(roleAnn.value()).noneMatch(v -> v.equalsIgnoreCase(loginUser.getRole()))) {
            throw new BizException(403, "无权限访问");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
