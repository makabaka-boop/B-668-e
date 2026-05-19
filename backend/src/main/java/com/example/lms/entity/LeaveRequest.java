package com.example.lms.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假申请实体类
 *
 * @author 系统
 * @since 2026-01-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 任课教师ID
     */
    private Long teacherId;

    /**
     * 辅导员ID
     */
    private Long counselorId;

    /**
     * 请假日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate leaveDate;

    /**
     * 请假类型 (SICK-病假/PERSONAL-事假)
     */
    private String leaveType;

    /**
     * 状态 (PENDING-待审核/APPROVED-通过/REJECTED-不通过)
     */
    private String status;

    /**
     * 请假原因
     */
    private String reason;

    /**
     * 附件URL
     */
    private String attachmentUrl;

    /**
     * 审核意见
     */
    private String auditRemark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditedAt;
}
