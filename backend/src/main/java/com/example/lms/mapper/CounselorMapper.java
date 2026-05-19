package com.example.lms.mapper;

import com.example.lms.entity.Counselor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 辅导员Mapper接口
 *
 * @author 系统
 * @since 2026-01-26
 */
@Mapper
public interface CounselorMapper {

    /**
     * 根据ID查询辅导员
     */
    Counselor selectById(@Param("id") Long id);

    /**
     * 根据用户名查询辅导员
     */
    Counselor selectByUsername(@Param("username") String username);

    /**
     * 插入辅导员
     */
    int insert(Counselor counselor);

    /**
     * 更新辅导员
     */
    int update(Counselor counselor);

    /**
     * 删除辅导员
     */
    int deleteById(@Param("id") Long id);
}
