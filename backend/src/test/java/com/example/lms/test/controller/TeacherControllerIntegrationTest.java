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
@DisplayName("教师控制器集成测试")
class TeacherControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;
    private String teacherToken;
    private String studentToken;
    private String counselorToken;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        teacherToken = login("teacher1", "TEACHER");
        studentToken = login("student1", "STUDENT");
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
    @DisplayName("获取教师课程列表 - 成功")
    void testGetTeacherCourses() throws Exception {
        mockMvc.perform(get("/api/teacher/courses")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].courseId").exists())
                .andExpect(jsonPath("$.data[0].courseName").exists());
    }

    @Test
    @DisplayName("获取教师课程列表 - 角色权限拦截 (学生)")
    void testGetTeacherCoursesForbiddenStudent() throws Exception {
        mockMvc.perform(get("/api/teacher/courses")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("获取教师课程列表 - 角色权限拦截 (辅导员)")
    void testGetTeacherCoursesForbiddenCounselor() throws Exception {
        mockMvc.perform(get("/api/teacher/courses")
                        .header("Authorization", "Bearer " + counselorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("查询课程请假记录 - 成功")
    void testGetLeaveRequests() throws Exception {
        mockMvc.perform(get("/api/teacher/leave-requests")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").exists());
    }

    @Test
    @DisplayName("查询课程请假记录 - 按课程筛选")
    void testGetLeaveRequestsByCourseId() throws Exception {
        mockMvc.perform(get("/api/teacher/leave-requests?courseId=1")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("查询课程请假记录 - 按状态筛选")
    void testGetLeaveRequestsByStatus() throws Exception {
        mockMvc.perform(get("/api/teacher/leave-requests?status=APPROVED")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("查询课程请假记录 - 分页")
    void testGetLeaveRequestsPagination() throws Exception {
        mockMvc.perform(get("/api/teacher/leave-requests?page=1&size=2")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(2));
    }

    @Test
    @DisplayName("查询课程出勤统计 - 成功")
    void testGetAttendanceStatistics() throws Exception {
        mockMvc.perform(get("/api/teacher/stats/attendance")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("查询课程出勤统计 - 按课程筛选")
    void testGetAttendanceStatisticsByCourseId() throws Exception {
        mockMvc.perform(get("/api/teacher/stats/attendance?courseId=1")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
