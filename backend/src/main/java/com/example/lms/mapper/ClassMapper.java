package com.example.lms.mapper;

import com.example.lms.entity.ClassEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 班级Mapper接口
 *
 * @author 系统
 * @since 2026-01-26
 */
@Mapper
public interface ClassMapper {

    /**
     * 根据ID查询班级
     */
    ClassEntity selectById(@Param("id") Long id);
}

