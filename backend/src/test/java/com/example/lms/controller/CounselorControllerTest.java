package com.example.lms.controller;

import com.example.lms.common.UserInfo;
import com.example.lms.dto.AuditRequest;
import com.example.lms.dto.StatisticsDTO;
import com.example.lms.entity.LeaveRequest;
import com.example.lms.service.AuthService;
import com.example.lms.service.LeaveRequestService;
import com.example.lms.service.StatisticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CounselorController.class)
class CounselorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private LeaveRequestService leaveRequestService;

    @MockBean
    private StatisticsService statisticsService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String counselorToken = "counselor-token";
    private final UserInfo counselorUser = new UserInfo(1L, "counselor1", "张辅导员", "COUNSELOR");

    @BeforeEach
    void setUp() {
        when(authService.validateToken(counselorToken)).thenReturn(true);
        when(authService.getUserInfo(counselorToken)).thenReturn(counselorUser);
    }

    @Nested
    @DisplayName("GET /api/counselor/leave-requests/pending")
    class GetPendingTests {

        @Test
        @DisplayName("查询待审核列表成功")
        void getPendingSuccess() throws Exception {
            Map<String, Object> result = new HashMap<>();
            result.put("list", List.of());
            result.put("total", 0);
            result.put("page", 1);
            result.put("size", 10);

            when(leaveRequestService.getPendingLeaveRequests(1L, 1, 10)).thenReturn(result);

            mockMvc.perform(get("/api/counselor/leave-requests/pending")
                            .header("Authorization", "Bearer " + counselorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("POST /api/counselor/leave-requests/{id}/audit")
    class AuditTests {

        @Test
        @DisplayName("审核通过成功")
        void auditApproveSuccess() throws Exception {
            doNothing().when(leaveRequestService).auditLeaveRequest(eq(1L), eq(1L), any(AuditRequest.class));

            AuditRequest request = new AuditRequest();
            request.setResult("APPROVED");
            request.setRemark("同意请假");

            mockMvc.perform(post("/api/counselor/leave-requests/1/audit")
                            .header("Authorization", "Bearer " + counselorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("审核拒绝成功")
        void auditRejectSuccess() throws Exception {
            doNothing().when(leaveRequestService).auditLeaveRequest(eq(1L), eq(1L), any(AuditRequest.class));

            AuditRequest request = new AuditRequest();
            request.setResult("REJECTED");
            request.setRemark("理由不充分");

            mockMvc.perform(post("/api/counselor/leave-requests/1/audit")
                            .header("Authorization", "Bearer " + counselorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("缺少审核结果字段返回400")
        void auditWithoutResult() throws Exception {
            AuditRequest request = new AuditRequest();

            mockMvc.perform(post("/api/counselor/leave-requests/1/audit")
                            .header("Authorization", "Bearer " + counselorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }

    @Nested
    @DisplayName("GET /api/counselor/leave-requests")
    class GetLeaveRequestsTests {

        @Test
        @DisplayName("查询辅导员请假记录成功")
        void getLeaveRequestsSuccess() throws Exception {
            Map<String, Object> result = new HashMap<>();
            result.put("list", List.of());
            result.put("total", 0);
            result.put("page", 1);
            result.put("size", 10);

            when(leaveRequestService.getCounselorLeaveRequests(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(1), eq(10)))
                    .thenReturn(result);

            mockMvc.perform(get("/api/counselor/leave-requests")
                            .header("Authorization", "Bearer " + counselorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/counselor/stats/by-student")
    class GetStudentStatisticsTests {

        @Test
        @DisplayName("查询学生请假统计成功")
        void getStudentStatisticsSuccess() throws Exception {
            StatisticsDTO dto = new StatisticsDTO("2021001001", "张三", 3);
            when(statisticsService.getStudentStatistics(eq(1L), isNull(), isNull(), isNull()))
                    .thenReturn(List.of(dto));

            mockMvc.perform(get("/api/counselor/stats/by-student")
                            .header("Authorization", "Bearer " + counselorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].studentNo").value("2021001001"))
                    .andExpect(jsonPath("$.data[0].leaveCount").value(3));
        }
    }
}
