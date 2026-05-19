package com.example.lms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课程实体类
 *
 * @author 系统
 * @since 2026-01-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 星期几 (1-5: 周一到周五)
     */
    private Integer weekday;

    /**
     * 第几节课 (1-5)
     */
    private Integer period;

    /**
     * 学期 (如: 2025-2026-1)
     */
    private String term;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
