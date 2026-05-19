package com.example.lms.controller;

import com.example.lms.BaseTest;
import com.example.lms.dto.LeaveRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("学生控制器测试")
public class StudentControllerTest extends BaseTest {

    @Test
    @DisplayName("获取学生课程列表")
    void testGetStudentCourses() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", getAuthHeader(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").isNotEmpty());
    }

    @Test
    @DisplayName("提交请假申请 - 病假成功")
    void testCreateLeaveRequestSickSuccess() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setCourseId(1L);
        dto.setLeaveDate(LocalDate.of(2026, 1, 13));
        dto.setLeaveType("SICK");
        dto.setReason("感冒发烧");
        dto.setAttachmentUrl("/uploads/test.jpg");

        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", getAuthHeader(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("提交请假申请 - 事假成功")
    void testCreateLeaveRequestPersonalSuccess() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setCourseId(2L);
        dto.setLeaveDate(LocalDate.of(2026, 1, 13));
        dto.setLeaveType("PERSONAL");
        dto.setReason("家中有事");

        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", getAuthHeader(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("提交请假申请 - 课程不存在")
    void testCreateLeaveRequestCourseNotFound() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setCourseId(999L);
        dto.setLeaveDate(LocalDate.of(2026, 1, 13));
        dto.setLeaveType("SICK");
        dto.setReason("感冒");

        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", getAuthHeader(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4101));
    }

    @Test
    @DisplayName("提交请假申请 - 未选该课程")
    void testCreateLeaveRequestNotEnrolled() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setCourseId(7L);
        dto.setLeaveDate(LocalDate.of(2026, 1, 15));
        dto.setLeaveType("SICK");
        dto.setReason("感冒");

        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", getAuthHeader(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4102));
    }

    @Test
    @DisplayName("提交请假申请 - 日期星期不匹配")
    void testCreateLeaveRequestWeekdayMismatch() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setCourseId(1L);
        dto.setLeaveDate(LocalDate.of(2026, 1, 14));
        dto.setLeaveType("SICK");
        dto.setReason("感冒");

        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", getAuthHeader(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4202));
    }

    @Test
    @DisplayName("提交请假申请 - 参数缺失")
    void testCreateLeaveRequestMissingParams() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();

        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", getAuthHeader(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("查询请假记录列表")
    void testGetLeaveRequests() throws Exception {
        mockMvc.perform(get("/api/student/leave-requests")
                        .header("Authorization", getAuthHeader(studentToken))
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @DisplayName("查询请假记录 - 按状态筛选")
    void testGetLeaveRequestsByStatus() throws Exception {
        mockMvc.perform(get("/api/student/leave-requests")
                        .header("Authorization", getAuthHeader(studentToken))
                        .param("status", "APPROVED")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("查询请假记录 - 按日期范围筛选")
    void testGetLeaveRequestsByDateRange() throws Exception {
        mockMvc.perform(get("/api/student/leave-requests")
                        .header("Authorization", getAuthHeader(studentToken))
                        .param("dateFrom", "2026-01-01")
                        .param("dateTo", "2026-01-31")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("查询请假详情")
    void testGetLeaveRequestById() throws Exception {
        mockMvc.perform(get("/api/student/leave-requests/1")
                        .header("Authorization", getAuthHeader(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.studentId").value(1));
    }

    @Test
    @DisplayName("查询请假详情 - 无权限访问他人请假")
    void testGetLeaveRequestByIdForbidden() throws Exception {
        String student2Token = loginAndGetToken("student2", "123456", "STUDENT");

        mockMvc.perform(get("/api/student/leave-requests/1")
                        .header("Authorization", getAuthHeader(student2Token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("查询请假详情 - 请假不存在")
    void testGetLeaveRequestNotFound() throws Exception {
        mockMvc.perform(get("/api/student/leave-requests/999")
                        .header("Authorization", getAuthHeader(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4103));
    }
}
