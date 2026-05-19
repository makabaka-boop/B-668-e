package com.example.lms.service;

import com.example.lms.dto.CourseWithTeacherDTO;
import com.example.lms.mapper.EnrollmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程服务
 *
 * @author 系统
 * @since 2026-01-26
 */
@Service
public class CourseService {

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Value("${lms.current-term:}")
    private String currentTerm;

    /**
     * 获取学生的课程列表
     */
    public List<CourseWithTeacherDTO> getStudentCourses(Long studentId) {
        return enrollmentMapper.selectStudentCourses(studentId, normalizeTerm(currentTerm));
    }

    /**
     * 获取教师的课程列表
     */
    public List<CourseWithTeacherDTO> getTeacherCourses(Long teacherId) {
        return enrollmentMapper.selectTeacherCourses(teacherId, normalizeTerm(currentTerm));
    }

    private static String normalizeTerm(String term) {
        if (term == null) {
            return null;
        }
        String trimmed = term.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
