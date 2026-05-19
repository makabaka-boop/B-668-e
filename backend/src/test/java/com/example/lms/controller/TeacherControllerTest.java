package com.example.lms.controller;

import com.example.lms.common.UserInfo;
import com.example.lms.dto.CourseWithTeacherDTO;
import com.example.lms.dto.StatisticsDTO;
import com.example.lms.service.AuthService;
import com.example.lms.service.CourseService;
import com.example.lms.service.LeaveRequestService;
import com.example.lms.service.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeacherController.class)
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private LeaveRequestService leaveRequestService;

    @MockBean
    private StatisticsService statisticsService;

    private final String teacherToken = "teacher-token";
    private final UserInfo teacherUser = new UserInfo(1L, "teacher1", "王老师", "TEACHER");

    @BeforeEach
    void setUp() {
        when(authService.validateToken(teacherToken)).thenReturn(true);
        when(authService.getUserInfo(teacherToken)).thenReturn(teacherUser);
    }

    @Nested
    @DisplayName("GET /api/teacher/courses")
    class GetCoursesTests {

        @Test
        @DisplayName("获取教师课程列表成功")
        void getCoursesSuccess() throws Exception {
            CourseWithTeacherDTO dto = new CourseWithTeacherDTO();
            dto.setCourseId(1L);
            dto.setCourseName("数据库原理");
            dto.setTeacherName("王老师");

            when(courseService.getTeacherCourses(1L)).thenReturn(List.of(dto));

            mockMvc.perform(get("/api/teacher/courses")
                            .header("Authorization", "Bearer " + teacherToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].courseName").value("数据库原理"));
        }
    }

    @Nested
    @DisplayName("GET /api/teacher/leave-requests")
    class GetLeaveRequestsTests {

        @Test
        @DisplayName("查询教师请假记录成功")
        void getLeaveRequestsSuccess() throws Exception {
            Map<String, Object> result = new HashMap<>();
            result.put("list", List.of());
            result.put("total", 0);
            result.put("page", 1);
            result.put("size", 10);

            when(leaveRequestService.getTeacherLeaveRequests(eq(1L), isNull(), isNull(), isNull(), isNull(), eq(1), eq(10)))
                    .thenReturn(result);

            mockMvc.perform(get("/api/teacher/leave-requests")
                            .header("Authorization", "Bearer " + teacherToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("按课程筛选请假记录")
        void getLeaveRequestsByCourse() throws Exception {
            Map<String, Object> result = new HashMap<>();
            result.put("list", List.of());
            result.put("total", 0);
            result.put("page", 1);
            result.put("size", 10);

            when(leaveRequestService.getTeacherLeaveRequests(eq(1L), eq(1L), isNull(), isNull(), isNull(), eq(1), eq(10)))
                    .thenReturn(result);

            mockMvc.perform(get("/api/teacher/leave-requests")
                            .param("courseId", "1")
                            .header("Authorization", "Bearer " + teacherToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/teacher/stats/attendance")
    class GetAttendanceStatsTests {

        @Test
        @DisplayName("查询出勤统计成功")
        void getAttendanceStatsSuccess() throws Exception {
            StatisticsDTO dto = new StatisticsDTO("2021001001", "张三", 2);
            when(statisticsService.getAttendanceStatistics(eq(1L), isNull(), isNull(), isNull()))
                    .thenReturn(List.of(dto));

            mockMvc.perform(get("/api/teacher/stats/attendance")
                            .header("Authorization", "Bearer " + teacherToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].studentNo").value("2021001001"))
                    .andExpect(jsonPath("$.data[0].leaveCount").value(2));
        }
    }
}
