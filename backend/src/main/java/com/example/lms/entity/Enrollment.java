package com.example.lms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 选课实体类
 *
 * @author 系统
 * @since 2026-01-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

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
     * 学期
     */
    private String term;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
