package com.example.lms.mapper;

import com.example.lms.dto.CourseWithTeacherDTO;
import com.example.lms.entity.Enrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 选课Mapper接口
 *
 * @author 系统
 * @since 2026-01-26
 */
@Mapper
public interface EnrollmentMapper {

    /**
     * 根据ID查询选课记录
     */
    Enrollment selectById(@Param("id") Long id);

    /**
     * 查询学生的课程列表（含教师信息）
     */
    List<CourseWithTeacherDTO> selectStudentCourses(@Param("studentId") Long studentId, @Param("term") String term);

    /**
     * 查询教师的课程列表
     */
    List<CourseWithTeacherDTO> selectTeacherCourses(@Param("teacherId") Long teacherId, @Param("term") String term);

    /**
     * 根据学生ID、课程ID和学期查询选课记录
     */
    Enrollment selectByStudentAndCourse(@Param("studentId") Long studentId,
                                       @Param("courseId") Long courseId,
                                       @Param("term") String term);

    /**
     * 插入选课记录
     */
    int insert(Enrollment enrollment);

    /**
     * 删除选课记录
     */
    int deleteById(@Param("id") Long id);
}
