package com.example.lms.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 审核请求DTO
 *
 * @author 系统
 * @since 2026-01-26
 */
@Data
public class AuditRequest {

    /**
     * 审核结果 (APPROVED/REJECTED)
     */
    @NotBlank(message = "审核结果不能为空")
    private String result;

    /**
     * 审核意见
     */
    private String remark;
}
