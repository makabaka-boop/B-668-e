package com.example.lms.mapper;

import com.example.lms.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 课程Mapper接口
 *
 * @author 系统
 * @since 2026-01-26
 */
@Mapper
public interface CourseMapper {

    /**
     * 根据ID查询课程
     */
    Course selectById(@Param("id") Long id);

    /**
     * 插入课程
     */
    int insert(Course course);

    /**
     * 更新课程
     */
    int update(Course course);

    /**
     * 删除课程
     */
    int deleteById(@Param("id") Long id);
}
