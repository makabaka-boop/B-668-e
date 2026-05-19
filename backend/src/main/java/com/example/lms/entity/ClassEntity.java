package com.example.lms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 班级实体类
 * 注意：使用 ClassEntity 避免与 java.lang.Class 冲突
 *
 * @author 系统
 * @since 2026-01-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassEntity {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 辅导员ID
     */
    private Long counselorId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
