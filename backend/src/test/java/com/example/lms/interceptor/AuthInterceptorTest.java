package com.example.lms.interceptor;

import com.example.lms.common.UserInfo;
import com.example.lms.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthInterceptorTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("Token验证测试")
    class TokenValidationTests {

        @Test
        @DisplayName("无Authorization头返回401")
        void noAuthorizationHeader() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("GET");
            request.setRequestURI("/api/student/courses");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = authInterceptor.preHandle(request, response, null);

            assertFalse(result);
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        }

        @Test
        @DisplayName("Authorization头不以Bearer开头返回401")
        void invalidAuthorizationFormat() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("GET");
            request.setRequestURI("/api/student/courses");
            request.addHeader("Authorization", "Basic abc123");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = authInterceptor.preHandle(request, response, null);

            assertFalse(result);
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        }

        @Test
        @DisplayName("无效Token返回401")
        void invalidToken() throws Exception {
            when(authService.validateToken("invalid-token")).thenReturn(false);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("GET");
            request.setRequestURI("/api/student/courses");
            request.addHeader("Authorization", "Bearer invalid-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = authInterceptor.preHandle(request, response, null);

            assertFalse(result);
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        }

        @Test
        @DisplayName("Token有效但用户信息为空返回401")
        void validTokenButNoUserInfo() throws Exception {
            when(authService.validateToken("token")).thenReturn(true);
            when(authService.getUserInfo("token")).thenReturn(null);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("GET");
            request.setRequestURI("/api/student/courses");
            request.addHeader("Authorization", "Bearer token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = authInterceptor.preHandle(request, response, null);

            assertFalse(result);
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        }
    }

    @Nested
    @DisplayName("角色权限测试")
    class RolePermissionTests {

        private void setupValidToken(String token, UserInfo userInfo) {
            when(authService.validateToken(token)).thenReturn(true);
            when(authService.getUserInfo(token)).thenReturn(userInfo);
        }

        @Test
        @DisplayName("学生访问学生接口成功")
        void studentAccessStudentApi() throws Exception {
            UserInfo studentUser = new UserInfo(1L, "student1", "张三", "STUDENT");
            setupValidToken("student-token", studentUser);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("GET");
            request.setRequestURI("/api/student/courses");
            request.addHeader("Authorization", "Bearer student-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = authInterceptor.preHandle(request, response, null);

            assertTrue(result);
            assertEquals(studentUser, request.getAttribute("currentUser"));
        }

        @Test
        @DisplayName("学生访问辅导员接口返回403")
        void studentAccessCounselorApi() throws Exception {
            UserInfo studentUser = new UserInfo(1L, "student1", "张三", "STUDENT");
            setupValidToken("student-token", studentUser);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("GET");
            request.setRequestURI("/api/counselor/leave-requests/pending");
            request.addHeader("Authorization", "Bearer student-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = authInterceptor.preHandle(request, response, null);

            assertFalse(result);
            assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        }

        @Test
        @DisplayName("学生访问教师接口返回403")
        void studentAccessTeacherApi() throws Exception {
            UserInfo studentUser = new UserInfo(1L, "student1", "张三", "STUDENT");
            setupValidToken("student-token", studentUser);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("GET");
            request.setRequestURI("/api/teacher/courses");
            request.addHeader("Authorization", "Bearer student-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = authInterceptor.preHandle(request, response, null);

            assertFalse(result);
            assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        }

        @Test
        @DisplayName("辅导员访问辅导员接口成功")
        void counselorAccessCounselorApi() throws Exception {
            UserInfo counselorUser = new UserInfo(1L, "counselor1", "张辅导员", "COUNSELOR");
            setupValidToken("counselor-token", counselorUser);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("GET");
            request.setRequestURI("/api/counselor/leave-requests/pending");
            request.addHeader("Authorization", "Bearer counselor-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = authInterceptor.preHandle(request, response, null);

            assertTrue(result);
        }

        @Test
        @DisplayName("辅导员访问学生接口返回403")
        void counselorAccessStudentApi() throws Exception {
            UserInfo counselorUser = new UserInfo(1L, "counselor1", "张辅导员", "COUNSELOR");
            setupValidToken("counselor-token", counselorUser);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("GET");
            request.setRequestURI("/api/student/courses");
            request.addHeader("Authorization", "Bearer counselor-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = authInterceptor.preHandle(request, response, null);

            assertFalse(result);
            assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        }

        @Test
        @DisplayName("教师访问教师接口成功")
        void teacherAccessTeacherApi() throws Exception {
            UserInfo teacherUser = new UserInfo(1L, "teacher1", "王老师", "TEACHER");
            setupValidToken("teacher-token", teacherUser);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("GET");
            request.setRequestURI("/api/teacher/courses");
            request.addHeader("Authorization", "Bearer teacher-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = authInterceptor.preHandle(request, response, null);

            assertTrue(result);
        }

        @Test
        @DisplayName("教师访问学生接口返回403")
        void teacherAccessStudentApi() throws Exception {
            UserInfo teacherUser = new UserInfo(1L, "teacher1", "王老师", "TEACHER");
            setupValidToken("teacher-token", teacherUser);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("GET");
            request.setRequestURI("/api/student/courses");
            request.addHeader("Authorization", "Bearer teacher-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = authInterceptor.preHandle(request, response, null);

            assertFalse(result);
            assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        }

        @Test
        @DisplayName("任意角色访问auth接口成功")
        void anyRoleAccessAuthApi() throws Exception {
            UserInfo studentUser = new UserInfo(1L, "student1", "张三", "STUDENT");
            setupValidToken("student-token", studentUser);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("GET");
            request.setRequestURI("/api/auth/current-user");
            request.addHeader("Authorization", "Bearer student-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = authInterceptor.preHandle(request, response, null);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("OPTIONS请求测试")
    class OptionsRequestTests {

        @Test
        @DisplayName("OPTIONS请求直接放行")
        void optionsRequestPasses() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("OPTIONS");
            request.setRequestURI("/api/student/courses");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = authInterceptor.preHandle(request, response, null);

            assertTrue(result);
        }
    }
}
