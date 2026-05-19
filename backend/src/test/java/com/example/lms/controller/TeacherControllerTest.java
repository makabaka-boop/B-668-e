package com.example.lms.controller;

import com.example.lms.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("教师控制器测试")
public class TeacherControllerTest extends BaseTest {

    @Test
    @DisplayName("获取教师课程列表")
    void testGetTeacherCourses() throws Exception {
        mockMvc.perform(get("/api/teacher/courses")
                        .header("Authorization", getAuthHeader(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").isNotEmpty());
    }

    @Test
    @DisplayName("获取课程请假列表")
    void testGetLeaveRequests() throws Exception {
        mockMvc.perform(get("/api/teacher/leave-requests")
                        .header("Authorization", getAuthHeader(teacherToken))
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("获取课程请假列表 - 按课程筛选")
    void testGetLeaveRequestsByCourse() throws Exception {
        mockMvc.perform(get("/api/teacher/leave-requests")
                        .header("Authorization", getAuthHeader(teacherToken))
                        .param("courseId", "1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("获取课程请假列表 - 按状态筛选")
    void testGetLeaveRequestsByStatus() throws Exception {
        mockMvc.perform(get("/api/teacher/leave-requests")
                        .header("Authorization", getAuthHeader(teacherToken))
                        .param("status", "APPROVED")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("获取课程请假列表 - 按日期范围筛选")
    void testGetLeaveRequestsByDateRange() throws Exception {
        mockMvc.perform(get("/api/teacher/leave-requests")
                        .header("Authorization", getAuthHeader(teacherToken))
                        .param("dateFrom", "2026-01-01")
                        .param("dateTo", "2026-01-31")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("获取出勤统计")
    void testGetAttendanceStatistics() throws Exception {
        mockMvc.perform(get("/api/teacher/stats/attendance")
                        .header("Authorization", getAuthHeader(teacherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("获取出勤统计 - 按课程筛选")
    void testGetAttendanceStatisticsByCourse() throws Exception {
        mockMvc.perform(get("/api/teacher/stats/attendance")
                        .header("Authorization", getAuthHeader(teacherToken))
                        .param("courseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("获取出勤统计 - 按日期范围筛选")
    void testGetAttendanceStatisticsByDateRange() throws Exception {
        mockMvc.perform(get("/api/teacher/stats/attendance")
                        .header("Authorization", getAuthHeader(teacherToken))
                        .param("dateFrom", "2026-01-01")
                        .param("dateTo", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("教师2获取课程列表")
    void testGetTeacher2Courses() throws Exception {
        String teacher2Token = loginAndGetToken("teacher2", "123456", "TEACHER");

        mockMvc.perform(get("/api/teacher/courses")
                        .header("Authorization", getAuthHeader(teacher2Token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("教师3获取课程列表")
    void testGetTeacher3Courses() throws Exception {
        String teacher3Token = loginAndGetToken("teacher3", "123456", "TEACHER");

        mockMvc.perform(get("/api/teacher/courses")
                        .header("Authorization", getAuthHeader(teacher3Token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
