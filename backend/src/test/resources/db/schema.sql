-- ============================================================
-- 无纸化学生请假管理系统 - H2 测试数据库表结构
-- ============================================================

-- ============================================================
-- 1. 辅导员表 (counselor)
-- ============================================================
DROP TABLE IF EXISTS leave_request;
DROP TABLE IF EXISTS enrollment;
DROP TABLE IF EXISTS course_teacher;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS class;
DROP TABLE IF EXISTS teacher;
DROP TABLE IF EXISTS counselor;

CREATE TABLE counselor (
  id BIGINT PRIMARY KEY IDENTITY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  name VARCHAR(50) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_counselor_username ON counselor(username);

-- ============================================================
-- 2. 教师表 (teacher)
-- ============================================================
CREATE TABLE teacher (
  id BIGINT PRIMARY KEY IDENTITY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  name VARCHAR(50) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_teacher_username ON teacher(username);

-- ============================================================
-- 3. 班级表 (class)
-- ============================================================
CREATE TABLE class (
  id BIGINT PRIMARY KEY IDENTITY,
  class_name VARCHAR(50) NOT NULL UNIQUE,
  counselor_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_class_counselor ON class(counselor_id);

-- ============================================================
-- 4. 学生表 (student)
-- ============================================================
CREATE TABLE student (
  id BIGINT PRIMARY KEY IDENTITY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  name VARCHAR(50) NOT NULL,
  student_no VARCHAR(30) NOT NULL UNIQUE,
  class_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_student_class ON student(class_id);
CREATE INDEX idx_student_no ON student(student_no);

-- ============================================================
-- 5. 课程表 (course)
-- ============================================================
CREATE TABLE course (
  id BIGINT PRIMARY KEY IDENTITY,
  course_name VARCHAR(100) NOT NULL,
  weekday TINYINT NOT NULL,
  period TINYINT NOT NULL,
  term VARCHAR(20) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_course_term ON course(term);
CREATE INDEX idx_course_time ON course(weekday, period);

-- ============================================================
-- 6. 课程-教师关联表 (course_teacher)
-- ============================================================
CREATE TABLE course_teacher (
  id BIGINT PRIMARY KEY IDENTITY,
  course_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (course_id, teacher_id)
);

CREATE INDEX idx_ct_course ON course_teacher(course_id);
CREATE INDEX idx_ct_teacher ON course_teacher(teacher_id);

-- ============================================================
-- 7. 选课表 (enrollment)
-- ============================================================
CREATE TABLE enrollment (
  id BIGINT PRIMARY KEY IDENTITY,
  student_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  term VARCHAR(20) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (student_id, course_id, term)
);

CREATE INDEX idx_enroll_student ON enrollment(student_id);
CREATE INDEX idx_enroll_course ON enrollment(course_id);
CREATE INDEX idx_enroll_teacher ON enrollment(teacher_id);

-- ============================================================
-- 8. 请假申请表 (leave_request)
-- ============================================================
CREATE TABLE leave_request (
  id BIGINT PRIMARY KEY IDENTITY,
  student_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  counselor_id BIGINT NOT NULL,
  leave_date DATE NOT NULL,
  leave_type VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  reason VARCHAR(500),
  attachment_url VARCHAR(255),
  audit_remark VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  audited_at TIMESTAMP NULL,
  UNIQUE (student_id, course_id, leave_date)
);

CREATE INDEX idx_lr_student ON leave_request(student_id);
CREATE INDEX idx_lr_course_date ON leave_request(course_id, leave_date);
CREATE INDEX idx_lr_teacher ON leave_request(teacher_id);
CREATE INDEX idx_lr_counselor_status ON leave_request(counselor_id, status);
CREATE INDEX idx_lr_status ON leave_request(status);
