package com.example.lms.controller;

import com.example.lms.common.UserInfo;
import com.example.lms.dto.CourseWithTeacherDTO;
import com.example.lms.dto.LeaveRequestDTO;
import com.example.lms.entity.LeaveRequest;
import com.example.lms.service.AuthService;
import com.example.lms.service.CourseService;
import com.example.lms.service.LeaveRequestService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private LeaveRequestService leaveRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String studentToken = "student-token";
    private final UserInfo studentUser = new UserInfo(1L, "student1", "张三", "STUDENT");

    @BeforeEach
    void setUp() {
        when(authService.validateToken(studentToken)).thenReturn(true);
        when(authService.getUserInfo(studentToken)).thenReturn(studentUser);
    }

    @Nested
    @DisplayName("GET /api/student/courses")
    class GetCoursesTests {

        @Test
        @DisplayName("获取学生课程列表成功")
        void getCoursesSuccess() throws Exception {
            CourseWithTeacherDTO dto = new CourseWithTeacherDTO();
            dto.setCourseId(1L);
            dto.setCourseName("数据库原理");
            dto.setTeacherName("王老师");

            when(courseService.getStudentCourses(1L)).thenReturn(List.of(dto));

            mockMvc.perform(get("/api/student/courses")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].courseName").value("数据库原理"));
        }
    }

    @Nested
    @DisplayName("POST /api/student/leave-requests")
    class CreateLeaveRequestTests {

        @Test
        @DisplayName("提交请假申请成功")
        void createLeaveRequestSuccess() throws Exception {
            when(leaveRequestService.createLeaveRequest(eq(1L), any(LeaveRequestDTO.class))).thenReturn(1L);

            LeaveRequestDTO dto = new LeaveRequestDTO();
            dto.setCourseId(1L);
            dto.setLeaveDate(LocalDate.of(2025, 9, 1));
            dto.setLeaveType("SICK");
            dto.setReason("感冒");

            mockMvc.perform(post("/api/student/leave-requests")
                            .header("Authorization", "Bearer " + studentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(1));
        }

        @Test
        @DisplayName("课程ID为空时返回400")
        void createLeaveRequestWithoutCourseId() throws Exception {
            LeaveRequestDTO dto = new LeaveRequestDTO();
            dto.setLeaveDate(LocalDate.of(2025, 9, 1));
            dto.setLeaveType("SICK");

            mockMvc.perform(post("/api/student/leave-requests")
                            .header("Authorization", "Bearer " + studentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }

    @Nested
    @DisplayName("GET /api/student/leave-requests")
    class GetLeaveRequestsTests {

        @Test
        @DisplayName("查询学生请假记录成功")
        void getLeaveRequestsSuccess() throws Exception {
            Map<String, Object> result = new HashMap<>();
            result.put("list", List.of());
            result.put("total", 0);
            result.put("page", 1);
            result.put("size", 10);

            when(leaveRequestService.getStudentLeaveRequests(eq(1L), isNull(), isNull(), isNull(), eq(1), eq(10)))
                    .thenReturn(result);

            mockMvc.perform(get("/api/student/leave-requests")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(0));
        }

        @Test
        @DisplayName("带状态筛选查询成功")
        void getLeaveRequestsWithStatus() throws Exception {
            Map<String, Object> result = new HashMap<>();
            result.put("list", List.of());
            result.put("total", 0);
            result.put("page", 1);
            result.put("size", 10);

            when(leaveRequestService.getStudentLeaveRequests(eq(1L), eq("PENDING"), isNull(), isNull(), eq(1), eq(10)))
                    .thenReturn(result);

            mockMvc.perform(get("/api/student/leave-requests")
                            .param("status", "PENDING")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/student/leave-requests/{id}")
    class GetLeaveRequestByIdTests {

        @Test
        @DisplayName("查询请假详情成功")
        void getLeaveRequestByIdSuccess() throws Exception {
            LeaveRequest leaveRequest = new LeaveRequest();
            leaveRequest.setId(1L);
            leaveRequest.setStudentId(1L);
            leaveRequest.setCourseId(1L);
            leaveRequest.setLeaveDate(LocalDate.of(2026, 1, 20));
            leaveRequest.setLeaveType("SICK");
            leaveRequest.setStatus("PENDING");
            leaveRequest.setReason("感冒");
            leaveRequest.setCreatedAt(LocalDateTime.now());

            when(leaveRequestService.getLeaveRequestByIdForStudent(1L, 1L)).thenReturn(leaveRequest);

            mockMvc.perform(get("/api/student/leave-requests/1")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1));
        }
    }
}
