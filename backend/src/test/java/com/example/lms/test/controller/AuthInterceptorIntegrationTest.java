package com.example.lms.test.controller;

import com.example.lms.dto.LoginRequest;
import com.example.lms.dto.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("认证拦截器集成测试")
class AuthInterceptorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;
    private String studentToken;
    private String teacherToken;
    private String counselorToken;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        studentToken = login("student1", "STUDENT");
        teacherToken = login("teacher1", "TEACHER");
        counselorToken = login("counselor1", "COUNSELOR");
    }

    private String login(String username, String roleType) throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword("123456");
        request.setRoleType(roleType);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponse loginResponse = objectMapper.readTree(response).path("data")
                .toObject(LoginResponse.class);
        return loginResponse.getToken();
    }

    @Test
    @DisplayName("无Token访问 - 返回401")
    void testNoAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/student/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("无效Token格式 - 返回401")
    void testInvalidAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", "InvalidFormat"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("已过期Token访问 - 返回401")
    void testExpiredToken() throws Exception {
        String expiredToken = "expired-token-that-does-not-exist";

        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("学生访问辅导员接口 - 返回403")
    void testStudentAccessCounselorApi() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests/pending")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("学生访问教师接口 - 返回403")
    void testStudentAccessTeacherApi() throws Exception {
        mockMvc.perform(get("/api/teacher/courses")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("教师访问学生接口 - 返回403")
    void testTeacherAccessStudentApi() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("教师访问辅导员接口 - 返回403")
    void testTeacherAccessCounselorApi() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests/pending")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("辅导员访问学生接口 - 返回403")
    void testCounselorAccessStudentApi() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", "Bearer " + counselorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("辅导员访问教师接口 - 返回403")
    void testCounselorAccessTeacherApi() throws Exception {
        mockMvc.perform(get("/api/teacher/courses")
                        .header("Authorization", "Bearer " + counselorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("登录接口不需要Token")
    void testLoginEndpointNoAuth() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("123456");
        request.setRoleType("STUDENT");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("登出接口需要有效Token")
    void testLogoutRequiresAuth() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("登出接口使用正确Token")
    void testLogoutWithValidToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());
    }
}
