package com.example.lms.controller;

import com.example.lms.common.Result;
import com.example.lms.common.UserInfo;
import com.example.lms.dto.CourseWithTeacherDTO;
import com.example.lms.dto.StatisticsDTO;
import com.example.lms.service.CourseService;
import com.example.lms.service.LeaveRequestService;
import com.example.lms.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 教师控制器
 *
 * @author 系统
 * @since 2026-01-26
 */
@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取教师的课程列表
     */
    @GetMapping("/courses")
    public Result<List<CourseWithTeacherDTO>> getCourses(@RequestAttribute("currentUser") UserInfo currentUser) {
        List<CourseWithTeacherDTO> courses = courseService.getTeacherCourses(currentUser.getUserId());
        return Result.success(courses);
    }

    /**
     * 查询课程的请假记录（分页）
     */
    @GetMapping("/leave-requests")
    public Result<Map<String, Object>> getLeaveRequests(
            @RequestAttribute("currentUser") UserInfo currentUser,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = leaveRequestService.getTeacherLeaveRequests(
                currentUser.getUserId(), courseId, status, dateFrom, dateTo, page, size);
        return Result.success(result);
    }

    /**
     * 统计课程出勤情况
     */
    @GetMapping("/stats/attendance")
    public Result<List<StatisticsDTO>> getAttendanceStatistics(
            @RequestAttribute("currentUser") UserInfo currentUser,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        List<StatisticsDTO> statistics = statisticsService.getAttendanceStatistics(
                currentUser.getUserId(), courseId, dateFrom, dateTo);
        return Result.success(statistics);
    }
}
