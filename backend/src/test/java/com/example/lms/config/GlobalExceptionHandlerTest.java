package com.example.lms.config;

import com.example.lms.BaseTest;
import com.example.lms.dto.LeaveRequestDTO;
import com.example.lms.dto.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("全局异常处理器测试")
public class GlobalExceptionHandlerTest extends BaseTest {

    @Test
    @DisplayName("参数验证异常 - 登录请求参数为空")
    void testValidationExceptionLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("");
        request.setRoleType("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("参数验证异常 - 请假申请参数缺失")
    void testValidationExceptionLeaveRequest() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();

        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", getAuthHeader(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("业务异常 - 用户不存在")
    void testBusinessExceptionUserNotFound() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("123456");
        request.setRoleType("STUDENT");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4002))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @DisplayName("业务异常 - 密码错误")
    void testBusinessExceptionWrongPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("wrongpassword");
        request.setRoleType("STUDENT");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4001))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("业务异常 - 课程不存在")
    void testBusinessExceptionCourseNotFound() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setCourseId(999L);
        dto.setLeaveDate(LocalDate.of(2026, 1, 13));
        dto.setLeaveType("SICK");

        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", getAuthHeader(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4101))
                .andExpect(jsonPath("$.message").value("课程不存在"));
    }

    @Test
    @DisplayName("业务异常 - 请假申请不存在")
    void testBusinessExceptionLeaveRequestNotFound() throws Exception {
        mockMvc.perform(get("/api/student/leave-requests/999")
                        .header("Authorization", getAuthHeader(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4103))
                .andExpect(jsonPath("$.message").value("请假申请不存在"));
    }

    @Test
    @DisplayName("业务异常 - 无权限访问")
    void testBusinessExceptionForbidden() throws Exception {
        String student2Token = loginAndGetToken("student2", "123456", "STUDENT");

        mockMvc.perform(get("/api/student/leave-requests/1")
                        .header("Authorization", getAuthHeader(student2Token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权限访问"));
    }

    @Test
    @DisplayName("业务异常 - 请假日期与课程时间不匹配")
    void testBusinessExceptionInvalidLeaveDate() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setCourseId(1L);
        dto.setLeaveDate(LocalDate.of(2026, 1, 14));
        dto.setLeaveType("SICK");

        mockMvc.perform(post("/api/student/leave-requests")
                        .header("Authorization", getAuthHeader(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4202))
                .andExpect(jsonPath("$.message").value("请假日期与课程时间不匹配"));
    }

    @Test
    @DisplayName("未授权异常 - 无Token访问")
    void testUnauthorizedException() throws Exception {
        mockMvc.perform(get("/api/student/courses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未登录或登录已失效"));
    }

    @Test
    @DisplayName("未授权异常 - 无效Token")
    void testUnauthorizedExceptionInvalidToken() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("无效的Token"));
    }

    @Test
    @DisplayName("权限不足异常 - 角色不匹配")
    void testForbiddenExceptionRoleMismatch() throws Exception {
        mockMvc.perform(get("/api/teacher/courses")
                        .header("Authorization", getAuthHeader(studentToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权限访问"));
    }

    @Test
    @DisplayName("统一响应格式验证 - 成功响应")
    void testUnifiedResponseFormatSuccess() throws Exception {
        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", getAuthHeader(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("统一响应格式验证 - 失败响应")
    void testUnifiedResponseFormatError() throws Exception {
        mockMvc.perform(get("/api/student/leave-requests/999")
                        .header("Authorization", getAuthHeader(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("业务异常 - 请假已审核")
    void testBusinessExceptionAlreadyAudited() throws Exception {
        com.example.lms.dto.AuditRequest request = new com.example.lms.dto.AuditRequest();
        request.setResult("APPROVED");

        mockMvc.perform(post("/api/counselor/leave-requests/1/audit")
                        .header("Authorization", getAuthHeader(counselorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4203))
                .andExpect(jsonPath("$.message").value("请假申请已审核，无法重复审核"));
    }
}
