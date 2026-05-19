package com.example.lms.test.controller;

import com.example.lms.dto.LoginRequest;
import com.example.lms.dto.LoginResponse;
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
@DisplayName("学生控制器集成测试")
class StudentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;
    private String studentToken;
    private String counselorToken;
    private String teacherToken;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        studentToken = login("student1", "STUDENT");
        counselorToken = login("counselor1", "COUNSELOR");
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
    @DisplayName("获取学生课程列表 - 成功")
    void testGetStudentCourses() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].courseId").exists())
                .andExpect(jsonPath("$.data[0].courseName").exists());
    }

    @Test
    @DisplayName("获取学生课程列表 - 未授权访问")
    void testGetStudentCoursesUnauthorized() throws Exception {
        mockMvc.perform(get("/api/student/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("获取学生课程列表 - 角色权限拦截 (辅导员)")
    void testGetStudentCoursesForbidden() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", "Bearer " + counselorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("获取学生课程列表 - 角色权限拦截 (教师)")
    void testGetStudentCoursesForbiddenTeacher() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("提交请假申请 - 成功")
    void testCreateLeaveRequest() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setCourseId(3L);
        dto.setLeaveDate(LocalDate.of(2026, 1, 27));
        dto.setLeaveType("SICK");
        dto.setReason("感冒发烧");

        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("提交请假申请 - 课程不存在")
    void testCreateLeaveRequestCourseNotFound() throws Exception {
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
                .andExpect(jsonPath("$.code").value(4101));
    }

    @Test
    @DisplayName("提交请假申请 - 未选该课程")
    void testCreateLeaveRequestEnrollmentNotFound() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setCourseId(8L);
        dto.setLeaveDate(LocalDate.of(2026, 1, 30));
        dto.setLeaveType("SICK");
        dto.setReason("感冒发烧");

        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4102));
    }

    @Test
    @DisplayName("提交请假申请 - 日期与课程时间不匹配")
    void testCreateLeaveRequestInvalidDate() throws Exception {
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
                .andExpect(jsonPath("$.code").value(4202));
    }

    @Test
    @DisplayName("提交请假申请 - 参数校验失败")
    void testCreateLeaveRequestValidationFailed() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setLeaveDate(LocalDate.of(2026, 1, 27));
        dto.setLeaveType("SICK");

        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("查询学生请假记录 - 成功")
    void testGetLeaveRequests() throws Exception {
        mockMvc.perform(get("/api/student/leave-requests")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").exists());
    }

    @Test
    @DisplayName("查询学生请假记录 - 按状态筛选")
    void testGetLeaveRequestsByStatus() throws Exception {
        mockMvc.perform(get("/api/student/leave-requests?status=PENDING")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("查询学生请假记录 - 分页")
    void testGetLeaveRequestsPagination() throws Exception {
        mockMvc.perform(get("/api/student/leave-requests?page=1&size=2")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(2));
    }

    @Test
    @DisplayName("查询请假详情 - 成功")
    void testGetLeaveRequestById() throws Exception {
        mockMvc.perform(get("/api/student/leave-requests/1")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("查询请假详情 - 请假申请不存在")
    void testGetLeaveRequestByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/student/leave-requests/99999")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4103));
    }

    @Test
    @DisplayName("查询请假详情 - 越权访问他人请假记录")
    void testGetLeaveRequestByIdForbidden() throws Exception {
        mockMvc.perform(get("/api/student/leave-requests/4")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }
}
