package com.example.lms.test.controller;

import com.example.lms.dto.LeaveRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("全局异常处理器集成测试")
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;
    private String studentToken;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        com.example.lms.dto.LoginRequest request = new com.example.lms.dto.LoginRequest();
        request.setUsername("student1");
        request.setPassword("123456");
        request.setRoleType("STUDENT");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        com.example.lms.dto.LoginResponse loginResponse = objectMapper.readTree(response).path("data")
                .toObject(com.example.lms.dto.LoginResponse.class);
        studentToken = loginResponse.getToken();
    }

    @Test
    @DisplayName("参数校验异常 - 请求体为空")
    void testValidationExceptionEmptyBody() throws Exception {
        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("参数校验异常 - 缺少必填字段")
    void testValidationExceptionMissingFields() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();

        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("请求参数错误 - 无效的日期格式")
    void testInvalidDateFormat() throws Exception {
        mockMvc.perform(get("/api/student/leave-requests?dateFrom=invalid-date")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("404异常 - 不存在的接口路径")
    void testNotFound() throws Exception {
        mockMvc.perform(get("/api/nonexistent-endpoint")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(status == 404 || status == 401,
                            "Expected 404 or 401 but got " + status);
                });
    }

    @Test
    @DisplayName("业务异常 - 课程不存在")
    void testBusinessExceptionCourseNotFound() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setCourseId(99999L);
        dto.setLeaveDate(LocalDate.of(2026, 1, 27));
        dto.setLeaveType("SICK");
        dto.setReason("感冒发烧");

        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4101))
                .andExpect(jsonPath("$.message").value("课程不存在"));
    }

    @Test
    @DisplayName("业务异常 - 请假日期无效")
    void testBusinessExceptionInvalidLeaveDate() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setCourseId(1L);
        dto.setLeaveDate(LocalDate.of(2026, 1, 28));
        dto.setLeaveType("SICK");
        dto.setReason("感冒发烧");

        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4202))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("业务异常 - 请假申请不存在")
    void testBusinessExceptionLeaveRequestNotFound() throws Exception {
        mockMvc.perform(get("/api/student/leave-requests/99999")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4103))
                .andExpect(jsonPath("$.message").value("请假申请不存在"));
    }
}
