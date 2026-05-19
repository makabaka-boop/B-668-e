package com.example.lms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课程-教师关联实体类
 *
 * @author 系统
 * @since 2026-01-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseTeacher {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
