package com.example.lms.interceptor;

import com.example.lms.common.ErrorCode;
import com.example.lms.common.UserInfo;
import com.example.lms.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证拦截器
 *
 * @author 系统
 * @since 2026-01-26
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    private static final String AUTH_HEADER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理OPTIONS请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 获取Token
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(AUTH_HEADER_PREFIX)) {
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
            return false;
        }

        String token = authorization.substring(AUTH_HEADER_PREFIX.length()); // 移除 "Bearer "

        // 验证Token
        if (!authService.validateToken(token)) {
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.INVALID_TOKEN.getMessage());
            return false;
        }

        // 获取用户信息并设置到请求属性中
        UserInfo userInfo = authService.getUserInfo(token);
        if (userInfo == null) {
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.INVALID_TOKEN.getMessage());
            return false;
        }

        // 基于路由前缀的角色鉴权
        String uri = request.getRequestURI();
        if (!isRoleAllowed(userInfo, uri)) {
            writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());
            return false;
        }

        request.setAttribute("currentUser", userInfo);

        return true;
    }

    private static boolean isRoleAllowed(UserInfo userInfo, String uri) {
        if (uri == null) {
            return true;
        }

        if (uri.startsWith("/api/student")) {
            return "STUDENT".equals(userInfo.getRoleType());
        }
        if (uri.startsWith("/api/counselor")) {
            return "COUNSELOR".equals(userInfo.getRoleType());
        }
        if (uri.startsWith("/api/teacher")) {
            return "TEACHER".equals(userInfo.getRoleType());
        }

        return true;
    }

    private static void writeJsonError(HttpServletResponse response, int httpStatus, String message) throws Exception {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + httpStatus + ",\"message\":\"" + message + "\"}");
    }
}
