package com.example.lms.mapper;

import com.example.lms.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 学生Mapper接口
 *
 * @author 系统
 * @since 2026-01-26
 */
@Mapper
public interface StudentMapper {

    /**
     * 根据ID查询学生
     */
    Student selectById(@Param("id") Long id);

    /**
     * 根据用户名查询学生
     */
    Student selectByUsername(@Param("username") String username);

    /**
     * 插入学生
     */
    int insert(Student student);

    /**
     * 更新学生
     */
    int update(Student student);

    /**
     * 删除学生
     */
    int deleteById(@Param("id") Long id);
}
