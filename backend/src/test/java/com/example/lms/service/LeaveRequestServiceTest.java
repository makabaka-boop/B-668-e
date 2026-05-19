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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestMapper leaveRequestMapper;

    @Mock
    private EnrollmentMapper enrollmentMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private ClassMapper classMapper;

    @InjectMocks
    private LeaveRequestService leaveRequestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("创建请假申请测试")
    class CreateLeaveRequestTests {

        private Course course;
        private Enrollment enrollment;
        private Student student;
        private ClassEntity classEntity;

        @BeforeEach
        void initTestData() {
            course = new Course();
            course.setId(1L);
            course.setCourseName("数据库原理");
            course.setWeekday(1);
            course.setPeriod(1);
            course.setTerm("2025-2026-1");

            enrollment = new Enrollment();
            enrollment.setId(1L);
            enrollment.setStudentId(1L);
            enrollment.setCourseId(1L);
            enrollment.setTeacherId(1L);
            enrollment.setTerm("2025-2026-1");

            student = new Student();
            student.setId(1L);
            student.setUsername("student1");
            student.setName("张三");
            student.setClassId(1L);

            classEntity = new ClassEntity();
            classEntity.setId(1L);
            classEntity.setClassName("计算机2021级1班");
            classEntity.setCounselorId(1L);
        }

        @Test
        @DisplayName("成功创建请假申请")
        void createLeaveRequestSuccess() {
            when(courseMapper.selectById(1L)).thenReturn(course);
            when(enrollmentMapper.selectByStudentAndCourse(1L, 1L, "2025-2026-1")).thenReturn(enrollment);
            when(leaveRequestMapper.countDuplicate(1L, 1L, LocalDate.of(2025, 9, 1))).thenReturn(0);
            when(studentMapper.selectById(1L)).thenReturn(student);
            when(classMapper.selectById(1L)).thenReturn(classEntity);
            when(leaveRequestMapper.insert(any(LeaveRequest.class))).thenReturn(1);

            LeaveRequestDTO dto = new LeaveRequestDTO();
            dto.setCourseId(1L);
            dto.setLeaveDate(LocalDate.of(2025, 9, 1));
            dto.setLeaveType("SICK");
            dto.setReason("感冒发烧");

            Long id = leaveRequestService.createLeaveRequest(1L, dto);

            verify(leaveRequestMapper).insert(any(LeaveRequest.class));
        }

        @Test
        @DisplayName("课程不存在时抛出异常")
        void courseNotFound() {
            when(courseMapper.selectById(999L)).thenReturn(null);

            LeaveRequestDTO dto = new LeaveRequestDTO();
            dto.setCourseId(999L);
            dto.setLeaveDate(LocalDate.of(2025, 9, 1));
            dto.setLeaveType("SICK");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> leaveRequestService.createLeaveRequest(1L, dto));
            assertEquals(ErrorCode.COURSE_NOT_FOUND.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("未选课时抛出异常")
        void enrollmentNotFound() {
            when(courseMapper.selectById(1L)).thenReturn(course);
            when(enrollmentMapper.selectByStudentAndCourse(1L, 1L, "2025-2026-1")).thenReturn(null);

            LeaveRequestDTO dto = new LeaveRequestDTO();
            dto.setCourseId(1L);
            dto.setLeaveDate(LocalDate.of(2025, 9, 1));
            dto.setLeaveType("SICK");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> leaveRequestService.createLeaveRequest(1L, dto));
            assertEquals(ErrorCode.ENROLLMENT_NOT_FOUND.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("请假日期不在学期内时抛出异常")
        void leaveDateNotInTerm() {
            when(courseMapper.selectById(1L)).thenReturn(course);
            when(enrollmentMapper.selectByStudentAndCourse(1L, 1L, "2025-2026-1")).thenReturn(enrollment);

            LeaveRequestDTO dto = new LeaveRequestDTO();
            dto.setCourseId(1L);
            dto.setLeaveDate(LocalDate.of(2025, 8, 1));
            dto.setLeaveType("SICK");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> leaveRequestService.createLeaveRequest(1L, dto));
            assertEquals(ErrorCode.INVALID_LEAVE_DATE.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("请假日期星期与课程不匹配时抛出异常")
        void leaveDateWeekdayMismatch() {
            when(courseMapper.selectById(1L)).thenReturn(course);
            when(enrollmentMapper.selectByStudentAndCourse(1L, 1L, "2025-2026-1")).thenReturn(enrollment);

            LeaveRequestDTO dto = new LeaveRequestDTO();
            dto.setCourseId(1L);
            dto.setLeaveDate(LocalDate.of(2025, 9, 2));
            dto.setLeaveType("SICK");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> leaveRequestService.createLeaveRequest(1L, dto));
            assertEquals(ErrorCode.INVALID_LEAVE_DATE.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("重复提交请假申请时抛出异常")
        void duplicateLeaveRequest() {
            when(courseMapper.selectById(1L)).thenReturn(course);
            when(enrollmentMapper.selectByStudentAndCourse(1L, 1L, "2025-2026-1")).thenReturn(enrollment);
            when(leaveRequestMapper.countDuplicate(1L, 1L, LocalDate.of(2025, 9, 1))).thenReturn(1);

            LeaveRequestDTO dto = new LeaveRequestDTO();
            dto.setCourseId(1L);
            dto.setLeaveDate(LocalDate.of(2025, 9, 1));
            dto.setLeaveType("SICK");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> leaveRequestService.createLeaveRequest(1L, dto));
            assertEquals(ErrorCode.DUPLICATE_LEAVE_REQUEST.getCode(), exception.getCode());
        }
    }

    @Nested
    @DisplayName("审核请假申请测试")
    class AuditLeaveRequestTests {

        @Test
        @DisplayName("成功审核通过")
        void auditApproveSuccess() {
            LeaveRequest leaveRequest = new LeaveRequest();
            leaveRequest.setId(1L);
            leaveRequest.setCounselorId(1L);
            leaveRequest.setStatus("PENDING");

            when(leaveRequestMapper.selectById(1L)).thenReturn(leaveRequest);
            when(leaveRequestMapper.update(any(LeaveRequest.class))).thenReturn(1);

            AuditRequest request = new AuditRequest();
            request.setResult("APPROVED");
            request.setRemark("同意请假");

            assertDoesNotThrow(() -> leaveRequestService.auditLeaveRequest(1L, 1L, request));

            verify(leaveRequestMapper).update(any(LeaveRequest.class));
        }

        @Test
        @DisplayName("审核不存在的请假申请抛出异常")
        void auditNonExistentRequest() {
            when(leaveRequestMapper.selectById(999L)).thenReturn(null);

            AuditRequest request = new AuditRequest();
            request.setResult("APPROVED");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> leaveRequestService.auditLeaveRequest(999L, 1L, request));
            assertEquals(ErrorCode.LEAVE_REQUEST_NOT_FOUND.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("非该班级辅导员审核时抛出权限异常")
        void auditByWrongCounselor() {
            LeaveRequest leaveRequest = new LeaveRequest();
            leaveRequest.setId(1L);
            leaveRequest.setCounselorId(1L);
            leaveRequest.setStatus("PENDING");

            when(leaveRequestMapper.selectById(1L)).thenReturn(leaveRequest);

            AuditRequest request = new AuditRequest();
            request.setResult("APPROVED");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> leaveRequestService.auditLeaveRequest(1L, 2L, request));
            assertEquals(ErrorCode.FORBIDDEN.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("重复审核时抛出异常")
        void auditAlreadyAuditedRequest() {
            LeaveRequest leaveRequest = new LeaveRequest();
            leaveRequest.setId(1L);
            leaveRequest.setCounselorId(1L);
            leaveRequest.setStatus("APPROVED");

            when(leaveRequestMapper.selectById(1L)).thenReturn(leaveRequest);

            AuditRequest request = new AuditRequest();
            request.setResult("REJECTED");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> leaveRequestService.auditLeaveRequest(1L, 1L, request));
            assertEquals(ErrorCode.LEAVE_REQUEST_ALREADY_AUDITED.getCode(), exception.getCode());
        }
    }

    @Nested
    @DisplayName("查询请假记录测试")
    class QueryLeaveRequestTests {

        @Test
        @DisplayName("学生查询自己的请假详情成功")
        void getLeaveRequestByIdForStudentSuccess() {
            LeaveRequest leaveRequest = new LeaveRequest();
            leaveRequest.setId(1L);
            leaveRequest.setStudentId(1L);

            when(leaveRequestMapper.selectById(1L)).thenReturn(leaveRequest);

            LeaveRequest result = leaveRequestService.getLeaveRequestByIdForStudent(1L, 1L);
            assertNotNull(result);
            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("学生不能查看他人请假详情")
        void getLeaveRequestByIdForOtherStudent() {
            LeaveRequest leaveRequest = new LeaveRequest();
            leaveRequest.setId(1L);
            leaveRequest.setStudentId(2L);

            when(leaveRequestMapper.selectById(1L)).thenReturn(leaveRequest);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> leaveRequestService.getLeaveRequestByIdForStudent(1L, 1L));
            assertEquals(ErrorCode.FORBIDDEN.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("查询不存在的请假申请抛出异常")
        void getNonExistentLeaveRequest() {
            when(leaveRequestMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> leaveRequestService.getLeaveRequestById(999L));
            assertEquals(ErrorCode.LEAVE_REQUEST_NOT_FOUND.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("学生分页查询请假记录")
        void getStudentLeaveRequestsPaginated() {
            when(leaveRequestMapper.selectByStudent(eq(1L), isNull(), isNull(), isNull(), eq(0), eq(10)))
                    .thenReturn(List.of());
            when(leaveRequestMapper.countByStudent(eq(1L), isNull(), isNull(), isNull())).thenReturn(0);

            Map<String, Object> result = leaveRequestService.getStudentLeaveRequests(1L, null, null, null, 1, 10);

            assertNotNull(result);
            assertEquals(0, result.get("total"));
            assertEquals(1, result.get("page"));
            assertEquals(10, result.get("size"));
        }

        @Test
        @DisplayName("辅导员分页查询待审核列表")
        void getPendingLeaveRequestsPaginated() {
            when(leaveRequestMapper.selectPendingByCounselor(1L, 0, 10)).thenReturn(List.of());
            when(leaveRequestMapper.countPendingByCounselor(1L)).thenReturn(0);

            Map<String, Object> result = leaveRequestService.getPendingLeaveRequests(1L, 1, 10);

            assertNotNull(result);
            assertEquals(0, result.get("total"));
        }
    }
}
