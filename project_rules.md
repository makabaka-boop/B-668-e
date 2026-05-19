# 无纸化学生请假管理系统 - 项目规范文档

## 1. 项目概述

### 1.1 项目定位
期末数据库大作业，重点保证"数据库设计合理 + 业务闭环可运行"。

### 1.2 技术栈
- **后端**: Java 17 + Spring Boot 3.x + MyBatis
- **前端**: Vue 3 + Vite + Element Plus
- **数据库**: MySQL 8.0
- **构建工具**: Maven (后端) + npm/pnpm (前端)
- **容器化**: Docker + Docker Compose

### 1.3 系统角色
- **STUDENT** (学生): 提交请假申请、查看请假记录
- **COUNSELOR** (辅导员): 审核请假申请、统计查询
- **TEACHER** (教师): 查看课程请假信息、生成出勤统计

## 2. 目录结构规范

```
leave-manage/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/lms/
│   │   │   │   ├── LeaveManagementApplication.java
│   │   │   │   ├── common/           # 通用类
│   │   │   │   │   ├── Result.java   # 统一返回结果
│   │   │   │   │   └── ErrorCode.java
│   │   │   │   ├── config/           # 配置类
│   │   │   │   │   ├── WebConfig.java
│   │   │   │   │   └── DataInitializer.java
│   │   │   │   ├── controller/       # 控制器
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── StudentController.java
│   │   │   │   │   ├── CounselorController.java
│   │   │   │   │   └── TeacherController.java
│   │   │   │   ├── service/          # 业务逻辑层
│   │   │   │   │   ├── AuthService.java
│   │   │   │   │   ├── LeaveRequestService.java
│   │   │   │   │   ├── CourseService.java
│   │   │   │   │   └── StatisticsService.java
│   │   │   │   ├── mapper/           # MyBatis Mapper
│   │   │   │   │   ├── StudentMapper.java
│   │   │   │   │   ├── TeacherMapper.java
│   │   │   │   │   ├── CounselorMapper.java
│   │   │   │   │   ├── CourseMapper.java
│   │   │   │   │   ├── EnrollmentMapper.java
│   │   │   │   │   └── LeaveRequestMapper.java
│   │   │   │   ├── entity/           # 实体类
│   │   │   │   │   ├── Student.java
│   │   │   │   │   ├── Teacher.java
│   │   │   │   │   ├── Counselor.java
│   │   │   │   │   ├── Class.java
│   │   │   │   │   ├── Course.java
│   │   │   │   │   ├── CourseTeacher.java
│   │   │   │   │   ├── Enrollment.java
│   │   │   │   │   └── LeaveRequest.java
│   │   │   │   ├── dto/              # 数据传输对象
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── LeaveRequestDTO.java
│   │   │   │   │   └── StatisticsDTO.java
│   │   │   │   └── interceptor/      # 拦截器
│   │   │   │       └── AuthInterceptor.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── mapper/           # MyBatis XML
│   │   │           ├── StudentMapper.xml
│   │   │           ├── TeacherMapper.xml
│   │   │           ├── CounselorMapper.xml
│   │   │           ├── CourseMapper.xml
│   │   │           ├── EnrollmentMapper.xml
│   │   │           └── LeaveRequestMapper.xml
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── assets/            # 静态资源
│   │   ├── components/        # 公共组件
│   │   ├── views/             # 页面组件
│   │   │   ├── Login.vue
│   │   │   ├── student/
│   │   │   │   ├── CourseList.vue
│   │   │   │   ├── LeaveApply.vue
│   │   │   │   └── LeaveRecords.vue
│   │   │   ├── counselor/
│   │   │   │   ├── PendingList.vue
│   │   │   │   └── Statistics.vue
│   │   │   └── teacher/
│   │   │       ├── CourseList.vue
│   │   │       ├── LeaveList.vue
│   │   │       └── AttendanceStats.vue
│   │   ├── router/            # 路由配置
│   │   │   └── index.js
│   │   ├── utils/             # 工具类
│   │   │   └── request.js
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   ├── vite.config.js
│   └── Dockerfile
├── scripts/                    # 数据库脚本
│   ├── 01_schema.sql          # 表结构
│   └── 02_init_data.sql       # 初始化数据
├── docs/                       # 文档
│   ├── database_design.md     # 数据库设计文档
│   ├── api_design.md          # API 设计文档
│   └── deployment.md          # 部署文档
├── docker-compose.yml         # Docker Compose 配置
├── docker-compose.dev.yml     # 开发环境配置
├── .gitignore
├── README.md
└── project_rules.md           # 本文件

```

## 3. 代码规范

### 3.1 命名规范

#### Java 命名
- **类名**: 大驼峰 (PascalCase)，如 `LeaveRequestService`
- **方法名**: 小驼峰 (camelCase)，如 `getLeaveRequestById`
- **常量**: 全大写+下划线，如 `MAX_FILE_SIZE`
- **包名**: 全小写，如 `com.example.lms.service`

#### 数据库命名
- **表名**: 全小写+下划线，如 `leave_request`
- **字段名**: 全小写+下划线，如 `student_id`
- **索引名**: `idx_表名_字段名`，如 `idx_leave_request_student`
- **唯一键**: `uk_表名_字段名`，如 `uk_student_username`

#### 前端命名
- **组件名**: 大驼峰，如 `LeaveApply.vue`
- **变量/方法**: 小驼峰，如 `leaveRequestList`
- **常量**: 全大写+下划线，如 `API_BASE_URL`

### 3.2 注释规范

#### Java 注释
```java
/**
 * 请假申请服务类
 *
 * @author 系统
 * @since 2026-01-26
 */
public class LeaveRequestService {

    /**
     * 创建请假申请
     *
     * @param request 请假申请信息
     * @return 申请ID
     */
    public Long createLeaveRequest(LeaveRequestDTO request) {
        // 实现逻辑
    }
}
```

#### 数据库注释
```sql
-- 请假申请表
CREATE TABLE leave_request (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  student_id BIGINT NOT NULL COMMENT '学生ID',
  -- ...
) COMMENT='请假申请表';
```

### 3.3 代码风格
- 使用 4 个空格缩进（不使用 Tab）
- 每行代码不超过 120 字符
- 方法长度不超过 50 行
- 类长度不超过 500 行

## 4. Git 规范

### 4.1 分支策略
- **main**: 主分支，保持稳定可运行
- **dev**: 开发分支
- **feature/xxx**: 功能分支
- **bugfix/xxx**: 修复分支

### 4.2 提交规范
使用约定式提交 (Conventional Commits):

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type 类型**:
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `style`: 代码格式调整（不影响功能）
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具链相关

**示例**:
```
feat(student): 添加请假申请功能

- 实现请假申请表单
- 添加文件上传功能
- 完成表单验证

Closes #123
```

## 5. API 规范

### 5.1 RESTful 设计原则
- 使用名词表示资源，如 `/api/leave-requests`
- 使用 HTTP 方法表示操作:
  - `GET`: 查询
  - `POST`: 创建
  - `PUT`: 完整更新
  - `PATCH`: 部分更新
  - `DELETE`: 删除

### 5.2 统一返回格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 业务数据
  }
}
```

**状态码约定**:
- `200`: 成功
- `400`: 请求参数错误
- `401`: 未认证
- `403`: 无权限
- `404`: 资源不存在
- `500`: 服务器错误

### 5.3 分页格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

### 5.4 API 路径规范
```
/api/auth/login                                    # 登录
/api/auth/logout                                   # 登出

/api/student/courses                               # 学生课程列表
/api/student/leave-requests                        # 学生请假申请（POST/GET）
/api/student/leave-requests/{id}                   # 请假详情

/api/counselor/leave-requests/pending              # 待审核列表
/api/counselor/leave-requests/{id}/audit           # 审核请假
/api/counselor/leave-requests                      # 查询请假记录
/api/counselor/stats/by-student                    # 学生统计

/api/teacher/courses                               # 教师课程列表
/api/teacher/leave-requests                        # 课程请假列表
/api/teacher/stats/attendance                      # 出勤统计
```

## 6. 数据库规范

### 6.1 表设计原则
- 每张表必须有主键 `id BIGINT AUTO_INCREMENT`
- 必须有 `created_at DATETIME DEFAULT CURRENT_TIMESTAMP`
- 更新时间字段使用 `updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP`
- 逻辑删除使用 `deleted TINYINT DEFAULT 0`

### 6.2 索引规范
- 主键自动创建聚簇索引
- 外键字段必须创建索引
- 查询条件字段创建普通索引
- 唯一约束字段创建唯一索引
- 联合索引遵循最左前缀原则

### 6.3 字段类型选择
- **ID**: `BIGINT`
- **状态/类型**: `VARCHAR(20)`
- **金额**: `DECIMAL(10,2)`
- **日期**: `DATE`
- **时间**: `DATETIME`
- **文本**: `VARCHAR(n)` 或 `TEXT`
- **布尔**: `TINYINT` (0/1)

### 6.4 数据完整性
- 使用外键约束（开发环境可选）
- 使用 NOT NULL 约束必填字段
- 使用 DEFAULT 设置默认值
- 使用 UNIQUE 约束唯一字段

## 7. 权限与数据范围规则

### 7.1 角色权限
| 角色 | 权限范围 |
|------|---------|
| STUDENT | 仅能访问自己的请假申请与记录 |
| COUNSELOR | 仅能访问自己负责班级的学生记录与申请 |
| TEACHER | 仅能访问自己授课课程的学生请假信息 |

### 7.2 数据访问控制
- 所有 API 必须验证用户身份
- 查询时必须添加数据范围过滤条件
- 禁止跨权限访问其他用户数据

### 7.3 认证方式
- 使用 Session 或简单 Token 认证（MVP 阶段）
- Token 存储在 HTTP Header: `Authorization: Bearer <token>`
- 登录后返回用户信息和角色

## 8. 文件上传规范

### 8.1 存储方式
- MVP 阶段使用本地文件存储
- 存储路径: `backend/uploads/`
- 文件命名: `{timestamp}_{uuid}.{ext}`

### 8.2 文件限制
- 允许类型: `jpg`, `png`, `pdf`
- 单文件大小: ≤ 5MB
- 文件名长度: ≤ 255 字符

### 8.3 访问方式
- 通过静态资源路径访问: `/uploads/{filename}`
- 返回给前端默认使用相对路径: `/uploads/{filename}`

## 9. 环境配置

### 9.1 开发环境
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/leave_manage?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root

server:
  port: 8081
```

### 9.2 生产环境
```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/leave_manage?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

server:
  port: 8081
```

### 9.3 前端环境变量
```javascript
// .env.development
VITE_API_BASE_URL=http://localhost:8081

// .env.production
VITE_API_BASE_URL=/api
```

## 10. 联调规范

### 10.1 跨域配置
后端配置 CORS:
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true);
    }
}
```

### 10.2 前端代理
```javascript
// vite.config.js
export default {
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
}
```

## 11. 测试规范

### 11.1 单元测试
- 使用 JUnit 5 + Mockito
- Service 层必须编写单元测试
- 测试覆盖率 > 60%

### 11.2 集成测试
- 使用 Spring Boot Test
- 测试关键业务流程

### 11.3 前端测试
- 使用 Vitest（可选）
- 测试关键组件和工具函数

## 12. 部署规范

### 12.1 Docker 构建
```bash
# 构建后端镜像
cd backend
docker build -t leave-manage-backend:latest .

# 构建前端镜像
cd frontend
docker build -t leave-manage-frontend:latest .
```

### 12.2 Docker Compose 启动
```bash
# 开发环境
docker-compose -f docker-compose.dev.yml up -d

# 生产环境
docker-compose up -d
```

### 12.3 数据库初始化
- 首次启动自动执行 `scripts/01_schema.sql`
- 自动执行 `scripts/02_init_data.sql` 初始化测试数据

## 13. 业务规则

### 13.1 学期与排课
- 系统维护"当前学期" (term)
- 课程固定排在周一~周五 (weekday=1..5)
- 每天第 1~5 节课 (period=1..5)

### 13.2 请假申请规则
- 学生必须已选该课程才能请假
- 请假日期的星期必须与课程星期相同
- 请假类型: SICK (病假) / PERSONAL (事假)
- 请假状态: PENDING (待审核) / APPROVED (通过) / REJECTED (不通过)

### 13.3 审核规则
- 仅辅导员可审核
- 审核时可填写审核意见
- 审核后状态不可再次修改

### 13.4 统计规则
- 教师统计: 仅统计 APPROVED 的请假次数
- 辅导员统计: 可按班级/学生/日期范围/类型/状态筛选

## 14. 安全规范

### 14.1 密码安全
- 使用 BCrypt 加密存储
- 密码长度 ≥ 6 位

### 14.2 SQL 注入防护
- 使用 MyBatis 参数化查询
- 禁止拼接 SQL

### 14.3 XSS 防护
- 前端输入验证
- 后端输出转义

### 14.4 文件上传安全
- 验证文件类型
- 限制文件大小
- 随机文件名

## 15. 性能优化

### 15.1 数据库优化
- 合理使用索引
- 避免 SELECT *
- 使用分页查询

### 15.2 缓存策略
- MVP 阶段不使用缓存
- 后续可考虑 Redis

### 15.3 前端优化
- 路由懒加载
- 图片压缩
- 按需引入组件库

## 16. 文档规范

### 16.1 必需文档
- README.md: 项目介绍、快速开始
- database_design.md: 数据库设计文档
- api_design.md: API 接口文档
- deployment.md: 部署文档

### 16.2 文档更新
- 代码变更时同步更新文档
- 使用 Markdown 格式
- 包含示例和截图

## 17. 验收标准

### 17.1 功能完整性
- [ ] 三类角色均可登录
- [ ] 学生可提交请假申请并上传材料
- [ ] 辅导员可审核请假申请
- [ ] 学生可查看审核结果
- [ ] 教师可查看课程请假信息
- [ ] 教师可查看出勤统计
- [ ] 辅导员可查看统计汇总

### 17.2 数据库设计
- [ ] ER 图完整清晰
- [ ] 表结构符合规范
- [ ] 索引设计合理
- [ ] 初始化数据完整

### 17.3 代码质量
- [ ] 代码符合命名规范
- [ ] 关键代码有注释
- [ ] 无明显 bug
- [ ] 可正常运行

### 17.4 文档完整性
- [ ] README 完整
- [ ] API 文档完整
- [ ] 数据库文档完整
- [ ] 部署文档完整

---

**文档版本**: v1.0
**最后更新**: 2026-01-26
**维护者**: 开发团队
