package com.example.lms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生实体类
 *
 * @author 系统
 * @since 2026-01-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码哈希
     */
    private String passwordHash;

    /**
     * 姓名
     */
    private String name;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
