package com.example.lms.controller;

import com.example.lms.common.Result;
import com.example.lms.common.UserInfo;
import com.example.lms.dto.CourseWithTeacherDTO;
import com.example.lms.dto.LeaveRequestDTO;
import com.example.lms.entity.LeaveRequest;
import com.example.lms.service.CourseService;
import com.example.lms.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 学生控制器
 *
 * @author 系统
 * @since 2026-01-26
 */
@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private LeaveRequestService leaveRequestService;

    /**
     * 获取学生的课程列表
     */
    @GetMapping("/courses")
    public Result<List<CourseWithTeacherDTO>> getCourses(@RequestAttribute("currentUser") UserInfo currentUser) {
        List<CourseWithTeacherDTO> courses = courseService.getStudentCourses(currentUser.getUserId());
        return Result.success(courses);
    }

    /**
     * 提交请假申请
     */
    @PostMapping("/leave-requests")
    public Result<Long> createLeaveRequest(@RequestAttribute("currentUser") UserInfo currentUser,
                                           @Validated @RequestBody LeaveRequestDTO dto) {
        Long id = leaveRequestService.createLeaveRequest(currentUser.getUserId(), dto);
        return Result.success(id);
    }

    /**
     * 查询学生的请假记录（分页）
     */
    @GetMapping("/leave-requests")
    public Result<Map<String, Object>> getLeaveRequests(
            @RequestAttribute("currentUser") UserInfo currentUser,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = leaveRequestService.getStudentLeaveRequests(
                currentUser.getUserId(), status, dateFrom, dateTo, page, size);
        return Result.success(result);
    }

    /**
     * 查询请假详情
     */
    @GetMapping("/leave-requests/{id}")
    public Result<LeaveRequest> getLeaveRequestById(@RequestAttribute("currentUser") UserInfo currentUser,
                                                    @PathVariable Long id) {
        LeaveRequest leaveRequest = leaveRequestService.getLeaveRequestByIdForStudent(currentUser.getUserId(), id);
        return Result.success(leaveRequest);
    }
}
