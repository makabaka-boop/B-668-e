package com.example.lms.controller;

import com.example.lms.BaseTest;
import com.example.lms.dto.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("认证控制器测试")
public class AuthControllerTest extends BaseTest {

    @Test
    @DisplayName("学生登录成功")
    void testStudentLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("123456");
        request.setRoleType("STUDENT");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("student1"))
                .andExpect(jsonPath("$.data.roleType").value("STUDENT"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @DisplayName("教师登录成功")
    void testTeacherLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("teacher1");
        request.setPassword("123456");
        request.setRoleType("TEACHER");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("teacher1"))
                .andExpect(jsonPath("$.data.roleType").value("TEACHER"));
    }

    @Test
    @DisplayName("辅导员登录成功")
    void testCounselorLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("counselor1");
        request.setPassword("123456");
        request.setRoleType("COUNSELOR");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("counselor1"))
                .andExpect(jsonPath("$.data.roleType").value("COUNSELOR"));
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void testLoginWrongPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("wrongpassword");
        request.setRoleType("STUDENT");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void testLoginUserNotFound() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("123456");
        request.setRoleType("STUDENT");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4002));
    }

    @Test
    @DisplayName("登录失败 - 无效的角色类型")
    void testLoginInvalidRole() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("123456");
        request.setRoleType("INVALID");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("登录失败 - 参数缺失")
    void testLoginMissingParams() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("");
        request.setRoleType("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("获取当前用户信息 - 学生")
    void testGetCurrentUserStudent() throws Exception {
        mockMvc.perform(get("/api/auth/current-user")
                        .header("Authorization", getAuthHeader(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("student1"))
                .andExpect(jsonPath("$.data.roleType").value("STUDENT"));
    }

    @Test
    @DisplayName("获取当前用户信息 - 教师")
    void testGetCurrentUserTeacher() throws Exception {
        mockMvc.perform(get("/api/auth/current-user")
                        .header("Authorization", getAuthHeader(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("teacher1"))
                .andExpect(jsonPath("$.data.roleType").value("TEACHER"));
    }

    @Test
    @DisplayName("获取当前用户信息 - 辅导员")
    void testGetCurrentUserCounselor() throws Exception {
        mockMvc.perform(get("/api/auth/current-user")
                        .header("Authorization", getAuthHeader(counselorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("counselor1"))
                .andExpect(jsonPath("$.data.roleType").value("COUNSELOR"));
    }

    @Test
    @DisplayName("登出成功")
    void testLogoutSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", getAuthHeader(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("登出后Token失效")
    void testTokenInvalidAfterLogout() throws Exception {
        String token = loginAndGetToken("student2", "123456", "STUDENT");

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", getAuthHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/auth/current-user")
                        .header("Authorization", getAuthHeader(token)))
                .andExpect(status().isUnauthorized());
    }
}
