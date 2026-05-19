package com.example.lms.service;

import com.example.lms.common.BusinessException;
import com.example.lms.common.ErrorCode;
import com.example.lms.common.UserInfo;
import com.example.lms.dto.LoginRequest;
import com.example.lms.dto.LoginResponse;
import com.example.lms.entity.Counselor;
import com.example.lms.entity.Student;
import com.example.lms.entity.Teacher;
import com.example.lms.mapper.CounselorMapper;
import com.example.lms.mapper.StudentMapper;
import com.example.lms.mapper.TeacherMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private TeacherMapper teacherMapper;

    @Mock
    private CounselorMapper counselorMapper;

    @InjectMocks
    private AuthService authService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final String rawPassword = "123456";
    private final String encodedPassword = encoder.encode(rawPassword);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("登录测试")
    class LoginTests {

        @Test
        @DisplayName("学生登录成功")
        void studentLoginSuccess() {
            Student student = new Student();
            student.setId(1L);
            student.setUsername("student1");
            student.setName("张三");
            student.setPasswordHash(encodedPassword);

            when(studentMapper.selectByUsername("student1")).thenReturn(student);

            LoginRequest request = new LoginRequest();
            request.setUsername("student1");
            request.setPassword(rawPassword);
            request.setRoleType("STUDENT");

            LoginResponse response = authService.login(request);

            assertNotNull(response.getToken());
            assertEquals(1L, response.getUserId());
            assertEquals("student1", response.getUsername());
            assertEquals("张三", response.getName());
            assertEquals("STUDENT", response.getRoleType());
        }

        @Test
        @DisplayName("教师登录成功")
        void teacherLoginSuccess() {
            Teacher teacher = new Teacher();
            teacher.setId(1L);
            teacher.setUsername("teacher1");
            teacher.setName("王老师");
            teacher.setPasswordHash(encodedPassword);

            when(teacherMapper.selectByUsername("teacher1")).thenReturn(teacher);

            LoginRequest request = new LoginRequest();
            request.setUsername("teacher1");
            request.setPassword(rawPassword);
            request.setRoleType("TEACHER");

            LoginResponse response = authService.login(request);

            assertNotNull(response.getToken());
            assertEquals("TEACHER", response.getRoleType());
        }

        @Test
        @DisplayName("辅导员登录成功")
        void counselorLoginSuccess() {
            Counselor counselor = new Counselor();
            counselor.setId(1L);
            counselor.setUsername("counselor1");
            counselor.setName("张辅导员");
            counselor.setPasswordHash(encodedPassword);

            when(counselorMapper.selectByUsername("counselor1")).thenReturn(counselor);

            LoginRequest request = new LoginRequest();
            request.setUsername("counselor1");
            request.setPassword(rawPassword);
            request.setRoleType("COUNSELOR");

            LoginResponse response = authService.login(request);

            assertNotNull(response.getToken());
            assertEquals("COUNSELOR", response.getRoleType());
        }

        @Test
        @DisplayName("用户不存在时抛出异常")
        void loginWithNonExistentUser() {
            when(studentMapper.selectByUsername("unknown")).thenReturn(null);

            LoginRequest request = new LoginRequest();
            request.setUsername("unknown");
            request.setPassword(rawPassword);
            request.setRoleType("STUDENT");

            BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));
            assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("密码错误时抛出异常")
        void loginWithWrongPassword() {
            Student student = new Student();
            student.setId(1L);
            student.setUsername("student1");
            student.setName("张三");
            student.setPasswordHash(encodedPassword);

            when(studentMapper.selectByUsername("student1")).thenReturn(student);

            LoginRequest request = new LoginRequest();
            request.setUsername("student1");
            request.setPassword("wrong_password");
            request.setRoleType("STUDENT");

            BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));
            assertEquals(ErrorCode.LOGIN_FAILED.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("无效角色类型时抛出异常")
        void loginWithInvalidRoleType() {
            LoginRequest request = new LoginRequest();
            request.setUsername("user1");
            request.setPassword(rawPassword);
            request.setRoleType("ADMIN");

            BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));
            assertEquals(ErrorCode.BAD_REQUEST.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("每次登录生成不同的Token")
        void differentTokenOnEachLogin() {
            Student student = new Student();
            student.setId(1L);
            student.setUsername("student1");
            student.setName("张三");
            student.setPasswordHash(encodedPassword);

            when(studentMapper.selectByUsername("student1")).thenReturn(student);

            LoginRequest request = new LoginRequest();
            request.setUsername("student1");
            request.setPassword(rawPassword);
            request.setRoleType("STUDENT");

            LoginResponse response1 = authService.login(request);
            LoginResponse response2 = authService.login(request);

            assertNotEquals(response1.getToken(), response2.getToken());
        }
    }

    @Nested
    @DisplayName("Token验证测试")
    class TokenTests {

        @Test
        @DisplayName("登录后Token有效")
        void tokenIsValidAfterLogin() {
            Student student = new Student();
            student.setId(1L);
            student.setUsername("student1");
            student.setName("张三");
            student.setPasswordHash(encodedPassword);

            when(studentMapper.selectByUsername("student1")).thenReturn(student);

            LoginRequest request = new LoginRequest();
            request.setUsername("student1");
            request.setPassword(rawPassword);
            request.setRoleType("STUDENT");

            LoginResponse response = authService.login(request);

            assertTrue(authService.validateToken(response.getToken()));
        }

        @Test
        @DisplayName("随机Token无效")
        void randomTokenIsInvalid() {
            assertFalse(authService.validateToken("random-invalid-token"));
        }

        @Test
        @DisplayName("登出后Token无效")
        void tokenIsInvalidAfterLogout() {
            Student student = new Student();
            student.setId(1L);
            student.setUsername("student1");
            student.setName("张三");
            student.setPasswordHash(encodedPassword);

            when(studentMapper.selectByUsername("student1")).thenReturn(student);

            LoginRequest request = new LoginRequest();
            request.setUsername("student1");
            request.setPassword(rawPassword);
            request.setRoleType("STUDENT");

            LoginResponse response = authService.login(request);
            String token = response.getToken();

            assertTrue(authService.validateToken(token));

            authService.logout(token);

            assertFalse(authService.validateToken(token));
        }

        @Test
        @DisplayName("根据Token获取用户信息")
        void getUserInfoByToken() {
            Student student = new Student();
            student.setId(1L);
            student.setUsername("student1");
            student.setName("张三");
            student.setPasswordHash(encodedPassword);

            when(studentMapper.selectByUsername("student1")).thenReturn(student);

            LoginRequest request = new LoginRequest();
            request.setUsername("student1");
            request.setPassword(rawPassword);
            request.setRoleType("STUDENT");

            LoginResponse response = authService.login(request);

            UserInfo userInfo = authService.getUserInfo(response.getToken());

            assertNotNull(userInfo);
            assertEquals(1L, userInfo.getUserId());
            assertEquals("student1", userInfo.getUsername());
            assertEquals("张三", userInfo.getName());
            assertEquals("STUDENT", userInfo.getRoleType());
        }

        @Test
        @DisplayName("无效Token返回null用户信息")
        void getUserInfoWithInvalidToken() {
            UserInfo userInfo = authService.getUserInfo("invalid-token");
            assertNull(userInfo);
        }
    }
}
