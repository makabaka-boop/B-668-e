package com.example.lms.mapper;

import com.example.lms.dto.StatisticsDTO;
import com.example.lms.entity.LeaveRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 请假申请Mapper接口
 *
 * @author 系统
 * @since 2026-01-26
 */
@Mapper
public interface LeaveRequestMapper {

    /**
     * 根据ID查询请假申请
     */
    LeaveRequest selectById(@Param("id") Long id);

    /**
     * 查询学生的请假记录（分页）
     */
    List<LeaveRequest> selectByStudent(@Param("studentId") Long studentId,
                                       @Param("status") String status,
                                       @Param("dateFrom") LocalDate dateFrom,
                                       @Param("dateTo") LocalDate dateTo,
                                       @Param("offset") Integer offset,
                                       @Param("limit") Integer limit);

    /**
     * 统计学生的请假记录总数
     */
    int countByStudent(@Param("studentId") Long studentId,
                      @Param("status") String status,
                      @Param("dateFrom") LocalDate dateFrom,
                      @Param("dateTo") LocalDate dateTo);

    /**
     * 查询辅导员待审核的请假申请（分页）
     */
    List<LeaveRequest> selectPendingByCounselor(@Param("counselorId") Long counselorId,
                                                @Param("offset") Integer offset,
                                                @Param("limit") Integer limit);

    /**
     * 统计辅导员待审核的请假申请总数
     */
    int countPendingByCounselor(@Param("counselorId") Long counselorId);

    /**
     * 查询辅导员的请假记录（分页，支持多条件筛选）
     */
    List<LeaveRequest> selectByCounselor(@Param("counselorId") Long counselorId,
                                         @Param("classId") Long classId,
                                         @Param("studentNo") String studentNo,
                                         @Param("status") String status,
                                         @Param("leaveType") String leaveType,
                                         @Param("dateFrom") LocalDate dateFrom,
                                         @Param("dateTo") LocalDate dateTo,
                                         @Param("offset") Integer offset,
                                         @Param("limit") Integer limit);

    /**
     * 统计辅导员的请假记录总数
     */
    int countByCounselor(@Param("counselorId") Long counselorId,
                        @Param("classId") Long classId,
                        @Param("studentNo") String studentNo,
                        @Param("status") String status,
                        @Param("leaveType") String leaveType,
                        @Param("dateFrom") LocalDate dateFrom,
                        @Param("dateTo") LocalDate dateTo);

    /**
     * 查询教师课程的请假记录（分页）
     */
    List<LeaveRequest> selectByTeacher(@Param("teacherId") Long teacherId,
                                       @Param("courseId") Long courseId,
                                       @Param("status") String status,
                                       @Param("dateFrom") LocalDate dateFrom,
                                       @Param("dateTo") LocalDate dateTo,
                                       @Param("offset") Integer offset,
                                       @Param("limit") Integer limit);

    /**
     * 统计教师课程的请假记录总数
     */
    int countByTeacher(@Param("teacherId") Long teacherId,
                      @Param("courseId") Long courseId,
                      @Param("status") String status,
                      @Param("dateFrom") LocalDate dateFrom,
                      @Param("dateTo") LocalDate dateTo);

    /**
     * 统计学生请假次数（按学生分组）
     */
    List<StatisticsDTO> selectStatisticsByStudent(@Param("counselorId") Long counselorId,
                                                  @Param("classId") Long classId,
                                                  @Param("dateFrom") LocalDate dateFrom,
                                                  @Param("dateTo") LocalDate dateTo);

    /**
     * 统计课程出勤情况（按学生分组）
     */
    List<StatisticsDTO> selectAttendanceStatistics(@Param("teacherId") Long teacherId,
                                                   @Param("courseId") Long courseId,
                                                   @Param("dateFrom") LocalDate dateFrom,
                                                   @Param("dateTo") LocalDate dateTo);

    /**
     * 检查是否存在重复的请假申请
     */
    int countDuplicate(@Param("studentId") Long studentId,
                      @Param("courseId") Long courseId,
                      @Param("leaveDate") LocalDate leaveDate);

    /**
     * 插入请假申请
     */
    int insert(LeaveRequest leaveRequest);

    /**
     * 更新请假申请
     */
    int update(LeaveRequest leaveRequest);

    /**
     * 删除请假申请
     */
    int deleteById(@Param("id") Long id);
}
