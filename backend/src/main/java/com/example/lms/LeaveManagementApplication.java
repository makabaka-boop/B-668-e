package com.example.lms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 无纸化学生请假管理系统 - 主应用类
 *
 * @author 系统
 * @since 2026-01-26
 */
@SpringBootApplication
@MapperScan("com.example.lms.mapper")
public class LeaveManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeaveManagementApplication.class, args);
        System.out.println("========================================");
        System.out.println("无纸化学生请假管理系统启动成功！");
        System.out.println("API 地址: http://localhost:8081");
        System.out.println("========================================");
    }
}
