package com.example.lms.test.controller;

import com.example.lms.dto.AuditRequest;
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
@DisplayName("辅导员控制器集成测试")
class CounselorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;
    private String counselorToken;
    private String studentToken;
    private String teacherToken;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        counselorToken = login("counselor1", "COUNSELOR");
        studentToken = login("student1", "STUDENT");
        teacherToken = login("teacher1", "TEACHER");
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
    @DisplayName("查询待审核请假申请 - 成功")
    void testGetPendingLeaveRequests() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests/pending")
                        .header("Authorization", "Bearer " + counselorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").exists());
    }

    @Test
    @DisplayName("查询待审核请假申请 - 分页")
    void testGetPendingLeaveRequestsPagination() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests/pending?page=1&size=2")
                        .header("Authorization", "Bearer " + counselorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(2));
    }

    @Test
    @DisplayName("查询待审核请假申请 - 角色权限拦截 (学生)")
    void testGetPendingLeaveRequestsForbiddenStudent() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests/pending")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("查询待审核请假申请 - 角色权限拦截 (教师)")
    void testGetPendingLeaveRequestsForbiddenTeacher() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests/pending")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("审核请假申请 - 通过")
    void testAuditLeaveRequestApproved() throws Exception {
        AuditRequest request = new AuditRequest();
        request.setResult("APPROVED");
        request.setRemark("同意");

        mockMvc.perform(post("/api/counselor/leave-requests/5/audit")
                        .header("Authorization", "Bearer " + counselorToken)
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
                        .header("Authorization", "Bearer " + counselorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("审核请假申请 - 不存在的申请")
    void testAuditLeaveRequestNotFound() throws Exception {
        AuditRequest request = new AuditRequest();
        request.setResult("APPROVED");
        request.setRemark("同意");

        mockMvc.perform(post("/api/counselor/leave-requests/99999/audit")
                        .header("Authorization", "Bearer " + counselorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4103));
    }

    @Test
    @DisplayName("审核请假申请 - 已审核的申请不能重复审核")
    void testAuditLeaveRequestAlreadyAudited() throws Exception {
        AuditRequest request = new AuditRequest();
        request.setResult("APPROVED");
        request.setRemark("同意");

        mockMvc.perform(post("/api/counselor/leave-requests/1/audit")
                        .header("Authorization", "Bearer " + counselorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4203));
    }

    @Test
    @DisplayName("审核请假申请 - 参数校验失败")
    void testAuditLeaveRequestValidationFailed() throws Exception {
        AuditRequest request = new AuditRequest();

        mockMvc.perform(post("/api/counselor/leave-requests/5/audit")
                        .header("Authorization", "Bearer " + counselorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("查询请假记录 - 成功")
    void testGetLeaveRequests() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests")
                        .header("Authorization", "Bearer " + counselorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").exists());
    }

    @Test
    @DisplayName("查询请假记录 - 按状态筛选")
    void testGetLeaveRequestsByStatus() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests?status=APPROVED")
                        .header("Authorization", "Bearer " + counselorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("查询请假记录 - 按班级筛选")
    void testGetLeaveRequestsByClassId() throws Exception {
        mockMvc.perform(get("/api/counselor/leave-requests?classId=1")
                        .header("Authorization", "Bearer " + counselorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("查询学生请假统计 - 成功")
    void testGetStudentStatistics() throws Exception {
        mockMvc.perform(get("/api/counselor/stats/by-student")
                        .header("Authorization", "Bearer " + counselorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("查询学生请假统计 - 按班级筛选")
    void testGetStudentStatisticsByClassId() throws Exception {
        mockMvc.perform(get("/api/counselor/stats/by-student?classId=1")
                        .header("Authorization", "Bearer " + counselorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
