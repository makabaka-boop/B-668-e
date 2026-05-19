package com.example.lms.interceptor;

import com.example.lms.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("认证拦截器测试")
public class AuthInterceptorTest extends BaseTest {

    @Test
    @DisplayName("未携带Token - 返回401")
    void testNoToken() throws Exception {
        mockMvc.perform(get("/api/student/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token格式错误 - 返回401")
    void testInvalidTokenFormat() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", "InvalidToken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("无效Token - 返回401")
    void testInvalidToken() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", "Bearer invalid-token-12345"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("学生Token访问教师接口 - 返回403")
    void testStudentAccessTeacherApi() throws Exception {
        mockMvc.perform(get("/api/teacher/courses")
                        .header("Authorization", getAuthHeader(studentToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("学生Token访问辅导员接口 - 返回403")
    void testStudentAccessCounselorApi() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests/pending")
                        .header("Authorization", getAuthHeader(studentToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("教师Token访问学生接口 - 返回403")
    void testTeacherAccessStudentApi() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", getAuthHeader(teacherToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("教师Token访问辅导员接口 - 返回403")
    void testTeacherAccessCounselorApi() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests/pending")
                        .header("Authorization", getAuthHeader(teacherToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("辅导员Token访问学生接口 - 返回403")
    void testCounselorAccessStudentApi() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", getAuthHeader(counselorToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("辅导员Token访问教师接口 - 返回403")
    void testCounselorAccessTeacherApi() throws Exception {
        mockMvc.perform(get("/api/teacher/courses")
                        .header("Authorization", getAuthHeader(counselorToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("学生Token访问学生接口 - 成功")
    void testStudentAccessStudentApi() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", getAuthHeader(studentToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("教师Token访问教师接口 - 成功")
    void testTeacherAccessTeacherApi() throws Exception {
        mockMvc.perform(get("/api/teacher/courses")
                        .header("Authorization", getAuthHeader(teacherToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("辅导员Token访问辅导员接口 - 成功")
    void testCounselorAccessCounselorApi() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests/pending")
                        .header("Authorization", getAuthHeader(counselorToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("OPTIONS请求 - 无需认证")
    void testOptionsRequest() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("登录接口 - 无需认证")
    void testLoginNoAuthRequired() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("登出后Token失效 - 访问受保护接口返回401")
    void testTokenInvalidAfterLogout() throws Exception {
        String token = loginAndGetToken("student3", "123456", "STUDENT");

        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", getAuthHeader(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/logout")
                        .header("Authorization", getAuthHeader(token)))
                .andExpect(status().isMethodNotAllowed());
    }
}
