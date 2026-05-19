package com.example.lms.common;

import lombok.Getter;

/**
 * 错误码枚举
 *
 * @author 系统
 * @since 2026-01-26
 */
@Getter
public enum ErrorCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已失效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),

    // 业务错误 4xx
    LOGIN_FAILED(4001, "用户名或密码错误"),
    USER_NOT_FOUND(4002, "用户不存在"),
    INVALID_TOKEN(4003, "无效的Token"),
    TOKEN_EXPIRED(4004, "Token已过期"),

    COURSE_NOT_FOUND(4101, "课程不存在"),
    ENROLLMENT_NOT_FOUND(4102, "选课记录不存在"),
    LEAVE_REQUEST_NOT_FOUND(4103, "请假申请不存在"),
    CLASS_NOT_FOUND(4104, "班级不存在"),

    DUPLICATE_LEAVE_REQUEST(4201, "该课程当天已提交请假申请"),
    INVALID_LEAVE_DATE(4202, "请假日期与课程时间不匹配"),
    LEAVE_REQUEST_ALREADY_AUDITED(4203, "请假申请已审核，无法重复审核"),

    FILE_UPLOAD_FAILED(4301, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(4302, "文件类型不允许"),
    FILE_SIZE_EXCEEDED(4303, "文件大小超过限制"),

    // 服务器错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    DATABASE_ERROR(5001, "数据库操作失败");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 构造方法
     */
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
