package com.example.lms.controller;

import com.example.lms.BaseTest;
import com.example.lms.dto.AuditRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("辅导员控制器测试")
public class CounselorControllerTest extends BaseTest {

    @Test
    @DisplayName("获取待审核请假列表")
    void testGetPendingLeaveRequests() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests/pending")
                        .header("Authorization", getAuthHeader(counselorToken))
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("审核请假申请 - 通过")
    void testAuditLeaveRequestApproved() throws Exception {
        AuditRequest request = new AuditRequest();
        request.setResult("APPROVED");
        request.setRemark("同意请假");

        mockMvc.perform(post("/api/counselor/leave-requests/5/audit")
                        .header("Authorization", getAuthHeader(counselorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("审核请假申请 - 拒绝")
    void testAuditLeaveRequestRejected() throws Exception {
        AuditRequest request = new AuditRequest();
        request.setResult("REJECTED");
        request.setRemark("理由不充分");

        mockMvc.perform(post("/api/counselor/leave-requests/6/audit")
                        .header("Authorization", getAuthHeader(counselorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("审核请假申请 - 无权限审核其他辅导员的请假")
    void testAuditLeaveRequestForbidden() throws Exception {
        String counselor2Token = loginAndGetToken("counselor2", "123456", "COUNSELOR");

        AuditRequest request = new AuditRequest();
        request.setResult("APPROVED");
        request.setRemark("同意");

        mockMvc.perform(post("/api/counselor/leave-requests/5/audit")
                        .header("Authorization", getAuthHeader(counselor2Token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("审核请假申请 - 请假不存在")
    void testAuditLeaveRequestNotFound() throws Exception {
        AuditRequest request = new AuditRequest();
        request.setResult("APPROVED");
        request.setRemark("同意");

        mockMvc.perform(post("/api/counselor/leave-requests/999/audit")
                        .header("Authorization", getAuthHeader(counselorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4103));
    }

    @Test
    @DisplayName("审核请假申请 - 已审核的请假不能重复审核")
    void testAuditLeaveRequestAlreadyAudited() throws Exception {
        AuditRequest request = new AuditRequest();
        request.setResult("APPROVED");
        request.setRemark("同意");

        mockMvc.perform(post("/api/counselor/leave-requests/1/audit")
                        .header("Authorization", getAuthHeader(counselorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4203));
    }

    @Test
    @DisplayName("审核请假申请 - 参数缺失")
    void testAuditLeaveRequestMissingParams() throws Exception {
        AuditRequest request = new AuditRequest();

        mockMvc.perform(post("/api/counselor/leave-requests/5/audit")
                        .header("Authorization", getAuthHeader(counselorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("查询请假记录 - 辅导员")
    void testGetLeaveRequests() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests")
                        .header("Authorization", getAuthHeader(counselorToken))
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("查询请假记录 - 按班级筛选")
    void testGetLeaveRequestsByClass() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests")
                        .header("Authorization", getAuthHeader(counselorToken))
                        .param("classId", "1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("查询请假记录 - 按状态筛选")
    void testGetLeaveRequestsByStatus() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests")
                        .header("Authorization", getAuthHeader(counselorToken))
                        .param("status", "PENDING")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("查询请假记录 - 按日期范围筛选")
    void testGetLeaveRequestsByDateRange() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests")
                        .header("Authorization", getAuthHeader(counselorToken))
                        .param("dateFrom", "2026-01-01")
                        .param("dateTo", "2026-01-31")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("学生请假统计")
    void testGetStudentStatistics() throws Exception {
        mockMvc.perform(get("/api/counselor/stats/by-student")
                        .header("Authorization", getAuthHeader(counselorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("学生请假统计 - 按班级筛选")
    void testGetStudentStatisticsByClass() throws Exception {
        mockMvc.perform(get("/api/counselor/stats/by-student")
                        .header("Authorization", getAuthHeader(counselorToken))
                        .param("classId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("学生请假统计 - 按日期范围筛选")
    void testGetStudentStatisticsByDateRange() throws Exception {
        mockMvc.perform(get("/api/counselor/stats/by-student")
                        .header("Authorization", getAuthHeader(counselorToken))
                        .param("dateFrom", "2026-01-01")
                        .param("dateTo", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
