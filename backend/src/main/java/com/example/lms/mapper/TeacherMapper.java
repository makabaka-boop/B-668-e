package com.example.lms.mapper;

import com.example.lms.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 教师Mapper接口
 *
 * @author 系统
 * @since 2026-01-26
 */
@Mapper
public interface TeacherMapper {

    /**
     * 根据ID查询教师
     */
    Teacher selectById(@Param("id") Long id);

    /**
     * 根据用户名查询教师
     */
    Teacher selectByUsername(@Param("username") String username);

    /**
     * 插入教师
     */
    int insert(Teacher teacher);

    /**
     * 更新教师
     */
    int update(Teacher teacher);

    /**
     * 删除教师
     */
    int deleteById(@Param("id") Long id);
}
