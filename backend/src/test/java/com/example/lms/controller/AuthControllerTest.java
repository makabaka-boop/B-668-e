package com.example.lms.controller;

import com.example.lms.common.BusinessException;
import com.example.lms.common.ErrorCode;
import com.example.lms.dto.LoginRequest;
import com.example.lms.dto.LoginResponse;
import com.example.lms.common.UserInfo;
import com.example.lms.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("登录成功返回200和token")
        void loginSuccess() throws Exception {
            LoginResponse response = new LoginResponse("test-token", 1L, "student1", "张三", "STUDENT");
            when(authService.login(any(LoginRequest.class))).thenReturn(response);

            LoginRequest request = new LoginRequest();
            request.setUsername("student1");
            request.setPassword("123456");
            request.setRoleType("STUDENT");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.token").value("test-token"))
                    .andExpect(jsonPath("$.data.roleType").value("STUDENT"));
        }

        @Test
        @DisplayName("用户名或密码错误返回业务错误码")
        void loginFailed() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.LOGIN_FAILED));

            LoginRequest request = new LoginRequest();
            request.setUsername("student1");
            request.setPassword("wrong");
            request.setRoleType("STUDENT");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(4001));
        }

        @Test
        @DisplayName("缺少必填字段返回400")
        void loginWithMissingFields() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("用户不存在返回业务错误码")
        void loginUserNotFound() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            LoginRequest request = new LoginRequest();
            request.setUsername("unknown");
            request.setPassword("123456");
            request.setRoleType("STUDENT");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(4002));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    class LogoutTests {

        @Test
        @DisplayName("登出成功返回200")
        void logoutSuccess() throws Exception {
            when(authService.validateToken("test-token")).thenReturn(true);
            when(authService.getUserInfo("test-token")).thenReturn(new UserInfo(1L, "student1", "张三", "STUDENT"));
            doNothing().when(authService).logout(anyString());

            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer test-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/auth/current-user")
    class CurrentUserTests {

        @Test
        @DisplayName("获取当前用户信息成功")
        void getCurrentUserSuccess() throws Exception {
            UserInfo userInfo = new UserInfo(1L, "student1", "张三", "STUDENT");
            when(authService.validateToken("test-token")).thenReturn(true);
            when(authService.getUserInfo("test-token")).thenReturn(userInfo);

            mockMvc.perform(get("/api/auth/current-user")
                            .header("Authorization", "Bearer test-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value("student1"))
                    .andExpect(jsonPath("$.data.roleType").value("STUDENT"));
        }
    }
}
