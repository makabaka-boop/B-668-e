package com.example.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程信息DTO（含教师信息）
 *
 * @author 系统
 * @since 2026-01-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseWithTeacherDTO {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 星期几
     */
    private Integer weekday;

    /**
     * 第几节课
     */
    private Integer period;

    /**
     * 学期
     */
    private String term;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 教师姓名
     */
    private String teacherName;
}
