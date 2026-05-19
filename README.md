# 无纸化学生请假管理系统

基于 **Java 17 + Spring Boot 3 + Vue 3 + Element Plus** 的全栈学生请假管理系统，作为期末数据库大作业，重点保证"数据库设计合理 + 业务闭环
可运行"。

## 原始需求

> 请你协助我完成期末数据库大作业，后端使用java17springboot前端使用vue3
> 需求如下
> A2.无纸化学生请假管理系统
> 业务场景描述：
> 1）每个班级有一位辅导员，一位辅导员管理若干个学生班级。
> 2）每位学生每个学期修读若干门课程，每门课程被固定安排在周一至周五的某个时段（第1~5节）。
> 3）每门课程可有多位教师讲授；学生修读某门课程就确定了一位任课教师。
> 4）学生因故不能上课，需线上向辅导员申请，并提交佐证材料。
> 5）系统向辅导员提示学生的请假申请，辅导员审核通过或不通过，并将结果返回学生。
> 6）请假类别包括病假和事假。
> 7）教师可查看学生自己讲授的各课程学生请假信息，统计平时出勤分数。
> 8）学生可查看自己的请假记录。
> 9）辅导员可查看统计所带班级和学生的请假记录。
> 写一个project_rules.md以便于管理

1. **启动服务**

```bash
docker compose up
```

2. **查看状态**

```bash
docker compose ps
```

3. **访问系统**

- 前端: <http://localhost:3000>
- 后端 API: <http://localhost:8081>

## 测试账号

### 辅导员

- `counselor1` / `123456` (负责计算机 2021 级 1 班、2 班)
- `counselor2` / `123456` (负责软件工程 2021 级 1 班)

### 教师

- `teacher1` / `123456` (王老师 - 教数据库原理、计算机网络)
- `teacher2` / `123456` (刘老师 - 教操作系统、算法设计)
- `teacher3` / `123456` (陈老师 - 教计算机网络、软件工程)

### 学生

- `student1` ~ `student8` / `123456`

### 核心功能

#### 学生端

- 查看已选课程列表
- 提交请假申请（病假/事假）
- 上传佐证材料（图片/PDF）
- 查看请假记录和审核结果

#### 辅导员端

- 查看待审核请假申请
- 审核请假（通过/不通过）
- 填写审核意见
- 统计查询（按班级/学生/日期范围）

#### 教师端

- 查看任教课程列表
- 查看课程学生请假信息
- 生成出勤统计报表
- 可视化展示请假次数（ECharts）

## 技术栈

### 后端

- **Java 17** - 编程语言
- **Spring Boot 3.x** - 应用框架
- **MyBatis** - ORM 框架
- **MySQL 8.0** - 数据库
- **Maven** - 构建工具
- **BCrypt** - 密码加密

### 前端

- **Vue 3** - 前端框架
- **Vite** - 构建工具
- **Element Plus** - UI 组件库
- **Axios** - HTTP 客户端
- **Vue Router** - 路由管理
- **ECharts** - 数据可视化

### 容器化

- **Docker** - 容器化
- **Docker Compose** - 容器编排

## 项目结构

```
leave-manage/
├── backend/                    # 后端项目（Spring Boot）
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/lms/
│   │   │   │   ├── entity/           # 实体类
│   │   │   │   ├── mapper/           # MyBatis Mapper
│   │   │   │   ├── service/          # 业务逻辑层
│   │   │   │   ├── controller/       # 控制器
│   │   │   │   ├── dto/              # 数据传输对象
│   │   │   │   ├── common/           # 通用类
│   │   │   │   ├── config/           # 配置类
│   │   │   │   └── interceptor/      # 拦截器
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── mapper/           # MyBatis XML
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                   # 前端项目（Vue 3）
│   ├── src/
│   │   ├── views/             # 页面组件
│   │   │   ├── Login.vue
│   │   │   ├── student/       # 学生端页面
│   │   │   ├── counselor/     # 辅导员端页面
│   │   │   └── teacher/       # 教师端页面
│   │   ├── router/            # 路由配置
│   │   ├── utils/             # 工具类
│   │   └── components/        # 公共组件
│   ├── package.json
│   ├── vite.config.js
│   └── Dockerfile
├── scripts/                    # 数据库脚本
│   ├── 01_schema.sql          # 表结构
│   └── 02_init_data.sql       # 初始化数据
├── docs/                       # 文档
│   ├── implementation_plan.md # 实施计划
│   ├── database_design.md     # 数据库设计
│   └── api_design.md          # API 设计
├── docker-compose.yml         # Docker Compose 配置
├── docker-compose.dev.yml     # 开发环境配置
├── project_rules.md           # 项目规范
└── README.md                  # 本文件
```

## 代码架构

### 后端架构

后端采用经典的三层架构，基于 Spring Boot 3.2 + MyBatis 构建：

┌─────────────────────────────────────────────────────────────┐
│ Controller 层 │
│ AuthController / StudentController / TeacherController │
│ CounselorController / FileUploadController │
├─────────────────────────────────────────────────────────────┤
│ Service 层 │
│ AuthService / LeaveRequestService / CourseService │
│ StatisticsService / FileUploadService │
├─────────────────────────────────────────────────────────────┤
│ Mapper 层 │
│ StudentMapper / TeacherMapper / CounselorMapper │
│ CourseMapper / LeaveRequestMapper / EnrollmentMapper │
├─────────────────────────────────────────────────────────────┤
│ MySQL 数据库 │
└─────────────────────────────────────────────────────────────┘

#### 核心模块说明

| 模块            | 路径                          | 说明                                         |
| --------------- | ----------------------------- | -------------------------------------------- |
| **entity**      | `com.example.lms.entity`      | JPA 实体类，与数据库表一一对应               |
| **mapper**      | `com.example.lms.mapper`      | MyBatis Mapper 接口，定义 SQL 操作           |
| **service**     | `com.example.lms.service`     | 业务逻辑层，处理核心业务                     |
| **controller**  | `com.example.lms.controller`  | REST API 控制器，处理 HTTP 请求              |
| **dto**         | `com.example.lms.dto`         | 数据传输对象，用于接口参数和返回值           |
| **common**      | `com.example.lms.common`      | 通用类：Result、ErrorCode、BusinessException |
| **config**      | `com.example.lms.config`      | 配置类：WebConfig、GlobalExceptionHandler    |
| **interceptor** | `com.example.lms.interceptor` | 拦截器：AuthInterceptor 认证鉴权             |

#### 认证鉴权机制

```
请求流程：
┌──────────┐    ┌─────────────────┐    ┌────────────┐    ┌────────────┐
│  Client  │───▶│ AuthInterceptor │───▶│ Controller │───▶│  Service   │
└──────────┘    └─────────────────┘    └────────────┘    └────────────┘
                       │
                       ▼
              1. 验证 Bearer Token
              2. 解析用户信息 (UserInfo)
              3. 基于 URI 前缀的角色鉴权
              4. 注入 currentUser 到 Request
```

- **Token 验证**：基于内存 Map 存储的简单 Token 机制
- **角色鉴权**：根据请求 URI 前缀 (`/api/student`、`/api/teacher`、`/api/counselor`) 自动校验角色
- **数据范围**：通过 SQL 关联查询实现数据权限控制

#### 统一响应格式

```java
{
  "code": 200,        // 状态码：200 成功，其他为错误码
  "message": "成功",  // 提示信息
  "data": { ... }     // 业务数据
}
```

### 前端架构

前端采用 Vue 3 组合式 API + Element Plus 构建：

```
┌─────────────────────────────────────────────────────────────┐
│                        App.vue                              │
├─────────────────────────────────────────────────────────────┤
│                     Vue Router                              │
│         /login  /student/*  /teacher/*  /counselor/*        │
├─────────────────────────────────────────────────────────────┤
│                    Layout 组件                              │
│    StudentLayout / TeacherLayout / CounselorLayout          │
├─────────────────────────────────────────────────────────────┤
│                    Views 页面                               │
│  Login / CourseList / LeaveApply / LeaveRecords / ...       │
├─────────────────────────────────────────────────────────────┤
│                    Utils 工具                               │
│              request.js (Axios) / date.js (dayjs)           │
└─────────────────────────────────────────────────────────────┘
```

#### 核心模块说明

| 模块                | 路径                   | 说明                                     |
| ------------------- | ---------------------- | ---------------------------------------- |
| **views/student**   | `src/views/student/`   | 学生端页面：课程列表、请假申请、请假记录 |
| **views/teacher**   | `src/views/teacher/`   | 教师端页面：课程列表、请假列表、出勤统计 |
| **views/counselor** | `src/views/counselor/` | 辅导员端页面：待审核列表、统计查询       |
| **components**      | `src/components/`      | 布局组件和公共组件（FilePreview）        |
| **router**          | `src/router/`          | 路由配置，含路由守卫                     |
| **utils**           | `src/utils/`           | 工具函数：Axios 封装、日期处理           |

#### 路由结构

```
/login                    # 登录页
/student                  # 学生端（需登录）
  ├── /courses            # 我的课程
  ├── /leave-apply        # 请假申请
  └── /leave-records      # 请假记录
/teacher                  # 教师端（需登录）
  ├── /courses            # 我的课程
  ├── /leave-list         # 请假列表
  └── /attendance-stats   # 出勤统计
/counselor                # 辅导员端（需登录）
  ├── /pending            # 待审核列表
  └── /statistics         # 统计查询
```

#### Axios 请求封装

```javascript
// 请求拦截：自动添加 Authorization Header
// 响应拦截：
//   - code !== 200 时自动提示错误
//   - 401/403 自动跳转登录页
//   - 网络异常统一处理
```

### 前后端交互

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│   Frontend   │  HTTP   │   Backend    │   SQL   │    MySQL     │
│   (Vue 3)    │◀───────▶│ (Spring Boot)│◀───────▶│   Database   │
└──────────────┘         └──────────────┘         └──────────────┘
      │                         │
      │ localStorage            │ Token Map (内存)
      │ - token                 │ - token -> UserInfo
      │ - userInfo              │
      ▼                         ▼
```

## 工程细节

### 依赖版本

#### 后端依赖 (Maven)

| 依赖                   | 版本   | 说明            |
| ---------------------- | ------ | --------------- |
| Spring Boot            | 3.2.1  | 应用框架        |
| MyBatis Spring Boot    | 3.0.3  | ORM 框架        |
| MySQL Connector        | 8.x    | 数据库驱动      |
| Lombok                 | -      | 代码简化        |
| Spring Security Crypto | -      | BCrypt 密码加密 |
| Commons FileUpload     | 1.5    | 文件上传        |
| Commons IO             | 2.15.1 | IO 工具         |

#### 前端依赖 (npm)

| 依赖         | 版本    | 说明        |
| ------------ | ------- | ----------- |
| Vue          | 3.4.15  | 前端框架    |
| Vite         | 5.0.11  | 构建工具    |
| Element Plus | 2.5.4   | UI 组件库   |
| Vue Router   | 4.2.5   | 路由管理    |
| Axios        | 1.6.5   | HTTP 客户端 |
| ECharts      | 5.4.3   | 数据可视化  |
| dayjs        | 1.11.19 | 日期处理    |

### 配置说明

#### 后端配置 (application.yml)

```yaml
server:
  port: 8081 # 服务端口

spring:
  datasource:
    url: jdbc:mysql://... # 数据库连接
    username: root
    password: xxxxxx

mybatis:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true # 下划线转驼峰

file:
  upload-dir: ./uploads # 文件上传目录
  max-size: 5242880 # 最大 5MB
  allowed-types: jpg,jpeg,png,pdf
```

#### 前端配置 (vite.config.js)

```javascript
export default {
	server: {
		port: 3000,
		proxy: {
			'/api': 'http://localhost:8081', // API 代理
			'/uploads': 'http://localhost:8081', // 文件代理
		},
	},
}
```

### Docker 部署架构

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Compose                           │
├─────────────────┬─────────────────┬─────────────────────────┤
│    frontend     │     backend     │         mysql           │
│   (Nginx:80)    │  (Java:8081)    │       (MySQL:3306)      │
│                 │                 │                         │
│  - 静态资源托管  │  - REST API     │  - 数据持久化           │
│  - 反向代理     │  - 文件上传     │  - 初始化脚本自动执行    │
└─────────────────┴─────────────────┴─────────────────────────┘
```

### 文件上传流程

```
1. 前端选择文件 (jpg/png/pdf, ≤5MB)
2. FormData 上传到 /api/upload
3. 后端校验文件类型和大小
4. 生成唯一文件名 (UUID + 原扩展名)
5. 保存到 uploads/ 目录
6. 返回可访问的 URL (/uploads/xxx.jpg)
7. 前端保存 URL 到请假申请表单
```

### 错误码定义

| 错误码 | 说明                |
| ------ | ------------------- |
| 200    | 成功                |
| 400    | 请求参数错误        |
| 401    | 未登录或 Token 失效 |
| 403    | 无权限访问          |
| 404    | 资源不存在          |
| 500    | 服务器内部错误      |
| 4001   | 用户名或密码错误    |
| 4002   | 用户不存在          |
| 4003   | Token 无效          |
| 4004   | Token 已过期        |

## 数据库设计

### ER 图

系统包含 8 张核心表：

1. **counselor** - 辅导员表
2. **teacher** - 教师表
3. **class** - 班级表
4. **student** - 学生表
5. **course** - 课程表
6. **course_teacher** - 课程-教师关联表
7. **enrollment** - 选课表
8. **leave_request** - 请假申请表

详细设计请参考 [数据库设计文档](docs/database_design.md)。

### 关键设计点

- **多对多关系**: 课程与教师通过 `course_teacher` 表关联
- **选课绑定**: 学生选课时必须指定任课教师
- **唯一约束**: 防止重复提交同一课程同一天的请假申请
- **索引优化**: 在高频查询字段上建立索引
- **数据范围**: 通过外键关联实现权限数据范围控制

## 快速开始

### 前置要求

- Docker 20.10+
- Docker Compose 2.0+

### 开发环境部署

1. **克隆项目**

```bash
git clone <repository-url>
cd leave-manage
```

1. **启动服务**

```bash
docker-compose -f docker-compose.dev.yml up -d
```

1. **查看日志**

```bash
docker-compose -f docker-compose.dev.yml logs -f
```

### 生产环境部署

1. **配置环境变量**

```bash
cp .env.example .env
vim .env
```

## API 接口

### 认证接口

```
POST /api/auth/login          # 登录
POST /api/auth/logout         # 登出
GET  /api/auth/current-user   # 获取当前用户信息
```

### 学生接口

```
GET  /api/student/courses                # 课程列表
POST /api/student/leave-requests         # 提交请假申请
GET  /api/student/leave-requests         # 请假记录列表
GET  /api/student/leave-requests/{id}    # 请假详情
```

### 辅导员接口

```
GET  /api/counselor/leave-requests/pending      # 待审核列表
POST /api/counselor/leave-requests/{id}/audit   # 审核请假
GET  /api/counselor/leave-requests              # 查询请假记录
GET  /api/counselor/stats/by-student            # 学生统计
```

### 教师接口

```
GET  /api/teacher/courses                # 课程列表
GET  /api/teacher/leave-requests         # 课程请假列表
GET  /api/teacher/stats/attendance       # 出勤统计
```

### 文件上传

```
POST /api/upload                         # 上传文件
```

说明：需要登录并携带 `Authorization: Bearer <token>`，上传成功后返回可访问的文件 URL（默认以 `/uploads/` 开头）。

详细 API 文档请参考 [API 设计文档](docs/api_design.md)。

## 业务规则

### 请假申请规则

1. 学生必须已选该课程才能请假
2. 请假日期的星期必须与课程星期相同
3. 同一课程同一天不能重复提交请假申请
4. 请假类型：SICK（病假）/ PERSONAL（事假）
5. 请假状态：PENDING（待审核）/ APPROVED（通过）/ REJECTED（不通过）

### 审核规则

1. 仅辅导员可审核所带班级学生的请假申请
2. 审核时可填写审核意见
3. 审核后状态不可再次修改

### 权限规则

- **学生**: 仅能访问自己的请假申请与记录
- **辅导员**: 仅能访问自己负责班级的学生记录与申请
- **教师**: 仅能访问自己授课课程的学生请假信息

## 开发指南

### 后端开发

1. **环境要求**
   - JDK 17+
   - Maven 3.8+
   - MySQL 8.0+

2. **本地运行**

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

1. **运行测试**

```bash
mvn test
```

### 前端开发

1. **环境要求**
   - Node.js 18+
   - npm 9+ 或 pnpm 8+

2. **本地运行**

```bash
cd frontend
npm install
npm run dev
```

1. **构建生产版本**

```bash
npm run build
```

## 项目规范

详细的项目规范请参考 [项目规范文档](project_rules.md)，包括：

- 代码规范（命名、注释、风格）
- Git 提交规范
- API 设计规范
- 数据库设计规范
- 文件上传规范
- 安全规范

## 验收标准

### 功能完整性

- [x] 三类角色均可登录
- [x] 学生可提交请假申请并上传材料
- [x] 辅导员可审核请假申请
- [x] 学生可查看审核结果
- [x] 教师可查看课程请假信息
- [x] 教师可查看出勤统计
- [x] 辅导员可查看统计汇总

### 数据库设计

- [x] ER 图完整清晰
- [x] 表结构符合规范
- [x] 索引设计合理
- [x] 初始化数据完整

### 代码质量

- [x] 代码符合命名规范
- [x] 关键代码有注释
- [ ] 无明显 bug
- [x] 可正常运行

### 文档完整性

- [x] README 完整
- [x] 数据库文档完整
- [x] 项目规范完整
- [x] 实施计划完整

## 后续优化方向

### 功能优化

- 消息通知（邮件/短信）
- 请假撤销功能
- 批量审核功能
- 导出报表功能
- 移动端适配

### 性能优化

- Redis 缓存
- 数据库读写分离
- CDN 加速
- 图片压缩

### 安全优化

- JWT Token 刷新机制
- 接口限流
- 日志审计
- 数据备份

## 常见问题

### 1. 数据库连接失败

- 检查 MySQL 服务是否启动
- 检查数据库配置是否正确
- 检查防火墙设置

### 2. 前端无法访问后端 API

- 检查后端服务是否启动
- 检查 CORS 配置
- 检查代理配置

### 3. 文件上传失败

- 检查文件大小是否超过限制（5MB）
- 检查文件类型是否允许（jpg/png/pdf）
- 检查上传目录权限

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 许可证

本项目仅用于学习和教学目的。

## 联系方式

如有问题，请提交 Issue 或联系开发团队。

---

**项目版本**: v1.0
**创建日期**: 2026-01-26
**最后更新**: 2026-01-26
**维护者**: 开发团队
