-- ============================================================
-- 无纸化学生请假管理系统 - 数据库表结构
-- 数据库: leave_manage
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS leave_manage DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE leave_manage;

-- 确保脚本以 UTF-8 执行
SET NAMES utf8mb4;

-- ============================================================
-- 1. 辅导员表 (counselor)
-- ============================================================
DROP TABLE IF EXISTS counselor;
CREATE TABLE counselor (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password_hash VARCHAR(100) NOT NULL COMMENT '密码哈希',
  name VARCHAR(50) NOT NULL COMMENT '姓名',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_counselor_username(username)
) COMMENT='辅导员表';

-- ============================================================
-- 2. 教师表 (teacher)
-- ============================================================
DROP TABLE IF EXISTS teacher;
CREATE TABLE teacher (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password_hash VARCHAR(100) NOT NULL COMMENT '密码哈希',
  name VARCHAR(50) NOT NULL COMMENT '姓名',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_teacher_username(username)
) COMMENT='教师表';

-- ============================================================
-- 3. 班级表 (class)
-- ============================================================
DROP TABLE IF EXISTS class;
CREATE TABLE class (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  class_name VARCHAR(50) NOT NULL UNIQUE COMMENT '班级名称',
  counselor_id BIGINT NOT NULL COMMENT '辅导员ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_class_counselor(counselor_id)
) COMMENT='班级表';

-- ============================================================
-- 4. 学生表 (student)
-- ============================================================
DROP TABLE IF EXISTS student;
CREATE TABLE student (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password_hash VARCHAR(100) NOT NULL COMMENT '密码哈希',
  name VARCHAR(50) NOT NULL COMMENT '姓名',
  student_no VARCHAR(30) NOT NULL UNIQUE COMMENT '学号',
  class_id BIGINT NOT NULL COMMENT '班级ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_student_class(class_id),
  INDEX idx_student_no(student_no)
) COMMENT='学生表';

-- ============================================================
-- 5. 课程表 (course)
-- ============================================================
DROP TABLE IF EXISTS course;
CREATE TABLE course (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
  weekday TINYINT NOT NULL COMMENT '星期几 (1-5: 周一到周五)',
  period TINYINT NOT NULL COMMENT '第几节课 (1-5)',
  term VARCHAR(20) NOT NULL COMMENT '学期 (如: 2025-2026-1)',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_course_term(term),
  INDEX idx_course_time(weekday, period)
) COMMENT='课程表';

-- ============================================================
-- 6. 课程-教师关联表 (course_teacher)
-- 一门课程可由多位教师讲授
-- ============================================================
DROP TABLE IF EXISTS course_teacher;
CREATE TABLE course_teacher (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  course_id BIGINT NOT NULL COMMENT '课程ID',
  teacher_id BIGINT NOT NULL COMMENT '教师ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_course_teacher(course_id, teacher_id),
  INDEX idx_ct_course(course_id),
  INDEX idx_ct_teacher(teacher_id)
) COMMENT='课程-教师关联表';

-- ============================================================
-- 7. 选课表 (enrollment)
-- 学生选课时必须指定任课教师
-- ============================================================
DROP TABLE IF EXISTS enrollment;
CREATE TABLE enrollment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  student_id BIGINT NOT NULL COMMENT '学生ID',
  course_id BIGINT NOT NULL COMMENT '课程ID',
  teacher_id BIGINT NOT NULL COMMENT '任课教师ID',
  term VARCHAR(20) NOT NULL COMMENT '学期',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_enrollment(student_id, course_id, term),
  INDEX idx_enroll_student(student_id),
  INDEX idx_enroll_course(course_id),
  INDEX idx_enroll_teacher(teacher_id)
) COMMENT='选课表';

-- ============================================================
-- 8. 请假申请表 (leave_request)
-- ============================================================
DROP TABLE IF EXISTS leave_request;
CREATE TABLE leave_request (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  student_id BIGINT NOT NULL COMMENT '学生ID',
  course_id BIGINT NOT NULL COMMENT '课程ID',
  teacher_id BIGINT NOT NULL COMMENT '任课教师ID',
  counselor_id BIGINT NOT NULL COMMENT '辅导员ID',
  leave_date DATE NOT NULL COMMENT '请假日期',
  leave_type VARCHAR(20) NOT NULL COMMENT '请假类型 (SICK-病假/PERSONAL-事假)',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态 (PENDING-待审核/APPROVED-通过/REJECTED-不通过)',
  reason VARCHAR(500) COMMENT '请假原因',
  attachment_url VARCHAR(255) COMMENT '附件URL',
  audit_remark VARCHAR(255) COMMENT '审核意见',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  audited_at DATETIME NULL COMMENT '审核时间',
  INDEX idx_lr_student(student_id),
  INDEX idx_lr_course_date(course_id, leave_date),
  INDEX idx_lr_teacher(teacher_id),
  INDEX idx_lr_counselor_status(counselor_id, status),
  INDEX idx_lr_status(status),
  UNIQUE KEY uk_leave_unique(student_id, course_id, leave_date)
) COMMENT='请假申请表';

-- ============================================================
-- 外键约束 (可选，开发环境建议不启用以提高灵活性)
-- ============================================================
-- ALTER TABLE class ADD CONSTRAINT fk_class_counselor FOREIGN KEY (counselor_id) REFERENCES counselor(id);
-- ALTER TABLE student ADD CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class(id);
-- ALTER TABLE course_teacher ADD CONSTRAINT fk_ct_course FOREIGN KEY (course_id) REFERENCES course(id);
-- ALTER TABLE course_teacher ADD CONSTRAINT fk_ct_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id);
-- ALTER TABLE enrollment ADD CONSTRAINT fk_enroll_student FOREIGN KEY (student_id) REFERENCES student(id);
-- ALTER TABLE enrollment ADD CONSTRAINT fk_enroll_course FOREIGN KEY (course_id) REFERENCES course(id);
-- ALTER TABLE enrollment ADD CONSTRAINT fk_enroll_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id);
-- ALTER TABLE leave_request ADD CONSTRAINT fk_lr_student FOREIGN KEY (student_id) REFERENCES student(id);
-- ALTER TABLE leave_request ADD CONSTRAINT fk_lr_course FOREIGN KEY (course_id) REFERENCES course(id);
-- ALTER TABLE leave_request ADD CONSTRAINT fk_lr_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id);
-- ALTER TABLE leave_request ADD CONSTRAINT fk_lr_counselor FOREIGN KEY (counselor_id) REFERENCES counselor(id);
