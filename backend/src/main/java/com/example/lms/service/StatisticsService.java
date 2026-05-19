package com.example.lms.service;

import com.example.lms.dto.StatisticsDTO;
import com.example.lms.mapper.LeaveRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计服务
 *
 * @author 系统
 * @since 2026-01-26
 */
@Service
public class StatisticsService {

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    /**
     * 统计学生请假次数（按学生分组）
     */
    public List<StatisticsDTO> getStudentStatistics(Long counselorId, Long classId,
                                                    LocalDate dateFrom, LocalDate dateTo) {
        return leaveRequestMapper.selectStatisticsByStudent(counselorId, classId, dateFrom, dateTo);
    }

    /**
     * 统计课程出勤情况（按学生分组）
     */
    public List<StatisticsDTO> getAttendanceStatistics(Long teacherId, Long courseId,
                                                       LocalDate dateFrom, LocalDate dateTo) {
        return leaveRequestMapper.selectAttendanceStatistics(teacherId, courseId, dateFrom, dateTo);
    }
}
