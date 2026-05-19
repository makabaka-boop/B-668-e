package com.example.lms.service;

import com.example.lms.common.BusinessException;
import com.example.lms.common.ErrorCode;
import com.example.lms.dto.AuditRequest;
import com.example.lms.dto.LeaveRequestDTO;
import com.example.lms.entity.ClassEntity;
import com.example.lms.entity.Course;
import com.example.lms.entity.Enrollment;
import com.example.lms.entity.LeaveRequest;
import com.example.lms.entity.Student;
import com.example.lms.mapper.ClassMapper;
import com.example.lms.mapper.CourseMapper;
import com.example.lms.mapper.EnrollmentMapper;
import com.example.lms.mapper.LeaveRequestMapper;
import com.example.lms.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请假申请服务
 *
 * @author 系统
 * @since 2026-01-26
 */
@Service
public class LeaveRequestService {

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ClassMapper classMapper;

    private static final int FALL_TERM_START_MONTH = 9;
    private static final int FALL_TERM_START_DAY = 1;
    private static final int FALL_TERM_END_MONTH = 1;
    private static final int FALL_TERM_END_DAY = 31;

    private static final int SPRING_TERM_START_MONTH = 2;
    private static final int SPRING_TERM_START_DAY = 1;
    private static final int SPRING_TERM_END_MONTH = 7;
    private static final int SPRING_TERM_END_DAY = 31;

    /**
     * 创建请假申请
     */
    @Transactional
    public Long createLeaveRequest(Long studentId, LeaveRequestDTO dto) {
        // 1. 查询课程信息
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        // 2. 验证学生是否选了该课程
        Enrollment enrollment = enrollmentMapper.selectByStudentAndCourse(
                studentId, dto.getCourseId(), course.getTerm());
        if (enrollment == null) {
            throw new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND);
        }

        // 3. 验证请假日期是否在当前学期内
        if (!isDateInTerm(dto.getLeaveDate(), course.getTerm())) {
            throw new BusinessException(ErrorCode.INVALID_LEAVE_DATE, "请假日期不在当前学期");
        }

        // 4. 验证请假日期的星期是否与课程星期匹配
        DayOfWeek dayOfWeek = dto.getLeaveDate().getDayOfWeek();
        int weekdayValue = dayOfWeek.getValue(); // 1=周一, 7=周日
        if (weekdayValue != course.getWeekday()) {
            throw new BusinessException(ErrorCode.INVALID_LEAVE_DATE);
        }

        // 5. 检查是否重复提交
        int duplicateCount = leaveRequestMapper.countDuplicate(
                studentId, dto.getCourseId(), dto.getLeaveDate());
        if (duplicateCount > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_LEAVE_REQUEST);
        }

        // 6. 查询学生信息获取辅导员ID
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (student.getClassId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "学生班级不能为空");
        }

        ClassEntity classEntity = classMapper.selectById(student.getClassId());
        if (classEntity == null) {
            throw new BusinessException(ErrorCode.CLASS_NOT_FOUND);
        }
        if (classEntity.getCounselorId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "班级辅导员不能为空");
        }
        Long counselorId = classEntity.getCounselorId();

        // 7. 创建请假申请
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setStudentId(studentId);
        leaveRequest.setCourseId(dto.getCourseId());
        leaveRequest.setTeacherId(enrollment.getTeacherId());
        leaveRequest.setCounselorId(counselorId);
        leaveRequest.setLeaveDate(dto.getLeaveDate());
        leaveRequest.setLeaveType(dto.getLeaveType());
        leaveRequest.setStatus("PENDING");
        leaveRequest.setReason(dto.getReason());
        leaveRequest.setAttachmentUrl(dto.getAttachmentUrl());
        leaveRequest.setCreatedAt(LocalDateTime.now());

        leaveRequestMapper.insert(leaveRequest);

        return leaveRequest.getId();
    }

    private static boolean isDateInTerm(LocalDate date, String term) {
        TermDateRange range = TermDateRange.fromTerm(term);
        if (range == null) {
            return true;
        }
        return !date.isBefore(range.startDate) && !date.isAfter(range.endDate);
    }

    private static final class TermDateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;

        private TermDateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        private static TermDateRange fromTerm(String term) {
            if (term == null) {
                return null;
            }
            String trimmed = term.trim();
            if (trimmed.isEmpty()) {
                return null;
            }

            String[] parts = trimmed.split("-");
            if (parts.length != 3) {
                return null;
            }

            try {
                int startYear = Integer.parseInt(parts[0]);
                int endYear = Integer.parseInt(parts[1]);
                int semester = Integer.parseInt(parts[2]);

                if (semester == 1) {
                    return new TermDateRange(
                            LocalDate.of(startYear, FALL_TERM_START_MONTH, FALL_TERM_START_DAY),
                            LocalDate.of(endYear, FALL_TERM_END_MONTH, FALL_TERM_END_DAY)
                    );
                }
                if (semester == 2) {
                    return new TermDateRange(
                            LocalDate.of(endYear, SPRING_TERM_START_MONTH, SPRING_TERM_START_DAY),
                            LocalDate.of(endYear, SPRING_TERM_END_MONTH, SPRING_TERM_END_DAY)
                    );
                }
            } catch (NumberFormatException ignored) {
                return null;
            }

            return null;
        }
    }

    /**
     * 查询学生的请假记录（分页）
     */
    public Map<String, Object> getStudentLeaveRequests(Long studentId, String status,
                                                       LocalDate dateFrom, LocalDate dateTo,
                                                       Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<LeaveRequest> list = leaveRequestMapper.selectByStudent(
                studentId, status, dateFrom, dateTo, offset, size);
        int total = leaveRequestMapper.countByStudent(studentId, status, dateFrom, dateTo);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    /**
     * 查询请假详情
     */
    public LeaveRequest getLeaveRequestById(Long id) {
        LeaveRequest leaveRequest = leaveRequestMapper.selectById(id);
        if (leaveRequest == null) {
            throw new BusinessException(ErrorCode.LEAVE_REQUEST_NOT_FOUND);
        }
        return leaveRequest;
    }

    /**
     * 学生查询自己的请假详情
     */
    public LeaveRequest getLeaveRequestByIdForStudent(Long studentId, Long id) {
        LeaveRequest leaveRequest = getLeaveRequestById(id);
        if (!studentId.equals(leaveRequest.getStudentId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return leaveRequest;
    }

    /**
     * 查询辅导员待审核的请假申请（分页）
     */
    public Map<String, Object> getPendingLeaveRequests(Long counselorId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<LeaveRequest> list = leaveRequestMapper.selectPendingByCounselor(counselorId, offset, size);
        int total = leaveRequestMapper.countPendingByCounselor(counselorId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    /**
     * 审核请假申请
     */
    @Transactional
    public void auditLeaveRequest(Long id, Long counselorId, AuditRequest request) {
        // 1. 查询请假申请
        LeaveRequest leaveRequest = leaveRequestMapper.selectById(id);
        if (leaveRequest == null) {
            throw new BusinessException(ErrorCode.LEAVE_REQUEST_NOT_FOUND);
        }

        // 2. 验证权限（是否是该班级辅导员）
        if (!leaveRequest.getCounselorId().equals(counselorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 3. 验证状态（是否已审核）
        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new BusinessException(ErrorCode.LEAVE_REQUEST_ALREADY_AUDITED);
        }

        // 4. 更新审核结果
        leaveRequest.setStatus(request.getResult());
        leaveRequest.setAuditRemark(request.getRemark());
        leaveRequest.setAuditedAt(LocalDateTime.now());

        leaveRequestMapper.update(leaveRequest);
    }

    /**
     * 查询辅导员的请假记录（分页，支持多条件筛选）
     */
    public Map<String, Object> getCounselorLeaveRequests(Long counselorId, Long classId,
                                                         String studentNo, String status,
                                                         String leaveType, LocalDate dateFrom,
                                                         LocalDate dateTo, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<LeaveRequest> list = leaveRequestMapper.selectByCounselor(
                counselorId, classId, studentNo, status, leaveType, dateFrom, dateTo, offset, size);
        int total = leaveRequestMapper.countByCounselor(
                counselorId, classId, studentNo, status, leaveType, dateFrom, dateTo);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    /**
     * 查询教师课程的请假记录（分页）
     */
    public Map<String, Object> getTeacherLeaveRequests(Long teacherId, Long courseId,
                                                       String status, LocalDate dateFrom,
                                                       LocalDate dateTo, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<LeaveRequest> list = leaveRequestMapper.selectByTeacher(
                teacherId, courseId, status, dateFrom, dateTo, offset, size);
        int total = leaveRequestMapper.countByTeacher(
                teacherId, courseId, status, dateFrom, dateTo);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }
}
