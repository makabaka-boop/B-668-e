package com.example.lms.service;

import com.example.lms.dto.StatisticsDTO;
import com.example.lms.mapper.LeaveRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StatisticsServiceTest {

    @Mock
    private LeaveRequestMapper leaveRequestMapper;

    @InjectMocks
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("辅导员查询学生请假统计")
    void getStudentStatistics() {
        StatisticsDTO dto1 = new StatisticsDTO("2021001001", "张三", 3);
        StatisticsDTO dto2 = new StatisticsDTO("2021001002", "李四", 1);

        when(leaveRequestMapper.selectStatisticsByStudent(eq(1L), isNull(), isNull(), isNull()))
                .thenReturn(List.of(dto1, dto2));

        List<StatisticsDTO> result = statisticsService.getStudentStatistics(1L, null, null, null);

        assertEquals(2, result.size());
        assertEquals("2021001001", result.get(0).getStudentNo());
        assertEquals(3, result.get(0).getLeaveCount());
    }

    @Test
    @DisplayName("辅导员按班级筛选统计")
    void getStudentStatisticsWithClassFilter() {
        when(leaveRequestMapper.selectStatisticsByStudent(eq(1L), eq(1L), isNull(), isNull()))
                .thenReturn(List.of());

        List<StatisticsDTO> result = statisticsService.getStudentStatistics(1L, 1L, null, null);
        assertNotNull(result);
    }

    @Test
    @DisplayName("教师查询出勤统计")
    void getAttendanceStatistics() {
        StatisticsDTO dto = new StatisticsDTO("2021001001", "张三", 2);
        when(leaveRequestMapper.selectAttendanceStatistics(eq(1L), isNull(), isNull(), isNull()))
                .thenReturn(List.of(dto));

        List<StatisticsDTO> result = statisticsService.getAttendanceStatistics(1L, null, null, null);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getLeaveCount());
    }

    @Test
    @DisplayName("教师按课程和日期筛选出勤统计")
    void getAttendanceStatisticsWithFilters() {
        LocalDate from = LocalDate.of(2025, 9, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);

        when(leaveRequestMapper.selectAttendanceStatistics(1L, 1L, from, to))
                .thenReturn(List.of());

        List<StatisticsDTO> result = statisticsService.getAttendanceStatistics(1L, 1L, from, to);
        assertNotNull(result);
    }
}
