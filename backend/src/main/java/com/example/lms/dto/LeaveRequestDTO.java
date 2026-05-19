package com.example.lms.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 请假申请DTO
 *
 * @author 系统
 * @since 2026-01-26
 */
@Data
public class LeaveRequestDTO {

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /**
     * 请假日期
     */
    @NotNull(message = "请假日期不能为空")
    private LocalDate leaveDate;

    /**
     * 请假类型 (SICK/PERSONAL)
     */
    @NotBlank(message = "请假类型不能为空")
    private String leaveType;

    /**
     * 请假原因
     */
    private String reason;

    /**
     * 附件URL
     */
    private String attachmentUrl;
}
