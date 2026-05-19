package com.example.lms.controller;

import com.example.lms.common.Result;
import com.example.lms.common.UserInfo;
import com.example.lms.dto.AuditRequest;
import com.example.lms.dto.StatisticsDTO;
import com.example.lms.service.LeaveRequestService;
import com.example.lms.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 辅导员控制器
 *
 * @author 系统
 * @since 2026-01-26
 */
@RestController
@RequestMapping("/api/counselor")
public class CounselorController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 查询待审核的请假申请（分页）
     */
    @GetMapping("/leave-requests/pending")
    public Result<Map<String, Object>> getPendingLeaveRequests(
            @RequestAttribute("currentUser") UserInfo currentUser,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = leaveRequestService.getPendingLeaveRequests(
                currentUser.getUserId(), page, size);
        return Result.success(result);
    }

    /**
     * 审核请假申请
     */
    @PostMapping("/leave-requests/{id}/audit")
    public Result<Void> auditLeaveRequest(@RequestAttribute("currentUser") UserInfo currentUser,
                                          @PathVariable Long id,
                                          @Validated @RequestBody AuditRequest request) {
        leaveRequestService.auditLeaveRequest(id, currentUser.getUserId(), request);
        return Result.success();
    }

    /**
     * 查询请假记录（分页，支持多条件筛选）
     */
    @GetMapping("/leave-requests")
    public Result<Map<String, Object>> getLeaveRequests(
            @RequestAttribute("currentUser") UserInfo currentUser,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String leaveType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = leaveRequestService.getCounselorLeaveRequests(
                currentUser.getUserId(), classId, studentNo, status, leaveType,
                dateFrom, dateTo, page, size);
        return Result.success(result);
    }

    /**
     * 统计学生请假次数
     */
    @GetMapping("/stats/by-student")
    public Result<List<StatisticsDTO>> getStudentStatistics(
            @RequestAttribute("currentUser") UserInfo currentUser,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        List<StatisticsDTO> statistics = statisticsService.getStudentStatistics(
                currentUser.getUserId(), classId, dateFrom, dateTo);
        return Result.success(statistics);
    }
}
