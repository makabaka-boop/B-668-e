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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证服务
 *
 * @author 系统
 * @since 2026-01-26
 */
@Service
public class AuthService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private CounselorMapper counselorMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Token存储（生产环境应使用Redis）
    private final Map<String, UserInfo> tokenStore = new ConcurrentHashMap<>();

    /**
     * 登录
     */
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String roleType = request.getRoleType();

        // 根据角色类型查询用户
        Long userId = null;
        String name = null;
        String passwordHash = null;

        switch (roleType) {
            case "STUDENT":
                Student student = studentMapper.selectByUsername(username);
                if (student == null) {
                    throw new BusinessException(ErrorCode.USER_NOT_FOUND);
                }
                userId = student.getId();
                name = student.getName();
                passwordHash = student.getPasswordHash();
                break;

            case "TEACHER":
                Teacher teacher = teacherMapper.selectByUsername(username);
                if (teacher == null) {
                    throw new BusinessException(ErrorCode.USER_NOT_FOUND);
                }
                userId = teacher.getId();
                name = teacher.getName();
                passwordHash = teacher.getPasswordHash();
                break;

            case "COUNSELOR":
                Counselor counselor = counselorMapper.selectByUsername(username);
                if (counselor == null) {
                    throw new BusinessException(ErrorCode.USER_NOT_FOUND);
                }
                userId = counselor.getId();
                name = counselor.getName();
                passwordHash = counselor.getPasswordHash();
                break;

            default:
                throw new BusinessException(ErrorCode.BAD_REQUEST, "无效的角色类型");
        }

        // 验证密码
        if (!encoder.matches(password, passwordHash)) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 生成Token
        String token = UUID.randomUUID().toString();

        // 存储用户信息
        UserInfo userInfo = new UserInfo(userId, username, name, roleType);
        tokenStore.put(token, userInfo);

        // 返回登录响应
        return new LoginResponse(token, userId, username, name, roleType);
    }

    /**
     * 登出
     */
    public void logout(String token) {
        tokenStore.remove(token);
    }

    /**
     * 根据Token获取用户信息
     */
    public UserInfo getUserInfo(String token) {
        return tokenStore.get(token);
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        return tokenStore.containsKey(token);
    }
}
