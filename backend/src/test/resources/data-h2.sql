INSERT INTO counselor (id, username, password_hash, name) VALUES
(1, 'counselor1', '$2a$10$WxduS1QPF4X2.jfrmdPH2ezft50TH2z1NO28863v4/lPNb2rGvcBy', '张辅导员'),
(2, 'counselor2', '$2a$10$WxduS1QPF4X2.jfrmdPH2ezft50TH2z1NO28863v4/lPNb2rGvcBy', '李辅导员');

INSERT INTO teacher (id, username, password_hash, name) VALUES
(1, 'teacher1', '$2a$10$WxduS1QPF4X2.jfrmdPH2ezft50TH2z1NO28863v4/lPNb2rGvcBy', '王老师'),
(2, 'teacher2', '$2a$10$WxduS1QPF4X2.jfrmdPH2ezft50TH2z1NO28863v4/lPNb2rGvcBy', '刘老师');

INSERT INTO class (id, class_name, counselor_id) VALUES
(1, '计算机2021级1班', 1),
(2, '计算机2021级2班', 2);

INSERT INTO student (id, username, password_hash, name, student_no, class_id) VALUES
(1, 'student1', '$2a$10$WxduS1QPF4X2.jfrmdPH2ezft50TH2z1NO28863v4/lPNb2rGvcBy', '张三', '2021001001', 1),
(2, 'student2', '$2a$10$WxduS1QPF4X2.jfrmdPH2ezft50TH2z1NO28863v4/lPNb2rGvcBy', '李四', '2021001002', 2);

INSERT INTO course (id, course_name, weekday, period, term) VALUES
(1, '数据库原理', 1, 1, '2025-2026-1'),
(2, '操作系统', 2, 3, '2025-2026-1'),
(3, '计算机网络', 3, 1, '2025-2026-1');

INSERT INTO enrollment (id, student_id, course_id, teacher_id, term) VALUES
(1, 1, 1, 1, '2025-2026-1'),
(2, 1, 2, 2, '2025-2026-1'),
(3, 2, 1, 1, '2025-2026-1'),
(4, 2, 3, 2, '2025-2026-1');

INSERT INTO leave_request (id, student_id, course_id, teacher_id, counselor_id, leave_date, leave_type, status, reason, attachment_url, audit_remark, audited_at) VALUES
(1, 1, 1, 1, 1, '2026-01-20', 'SICK', 'APPROVED', '感冒发烧', '/uploads/cert.jpg', '同意', '2026-01-19 10:30:00'),
(2, 2, 1, 1, 2, '2026-01-21', 'PERSONAL', 'PENDING', '家中有事', NULL, NULL, NULL),
(3, 1, 2, 2, 1, '2026-01-22', 'SICK', 'PENDING', '头痛', '/uploads/cert2.jpg', NULL, NULL);
