package com.example.lms.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 *
 * @author 系统
 * @since 2026-01-26
 */
@Data
public class LoginRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 角色类型 (STUDENT/TEACHER/COUNSELOR)
     */
    @NotBlank(message = "角色类型不能为空")
    private String roleType;
}
