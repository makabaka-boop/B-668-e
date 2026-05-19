# 自动化测试指南

## 目录

1. [项目概览](#项目概览)
2. [技术栈](#技术栈)
3. [测试策略](#测试策略)
4. [快速开始](#快速开始)
5. [后端测试](#后端测试)
6. [前端测试](#前端测试)
7. [Docker 测试](#docker-测试)
8. [测试报告](#测试报告)
9. [常见问题](#常见问题)

## 项目概览

本项目是一个学生请假管理系统，包含学生、教师、辅导员三种角色。测试覆盖了系统的核心功能模块，包括登录鉴权、请假申请、审核流程、统计查询等。

## 技术栈

### 后端测试
- **测试框架**: JUnit 5 (Jupiter)
- **Web 测试**: Spring Boot Test + MockMvc
- **Mock 框架**: Mockito
- **数据库**: H2 内存数据库
- **构建工具**: Maven

### 前端测试
- **测试框架**: Vitest 1.x
- **组件测试**: Vue Test Utils 2.x
- **浏览器环境**: jsdom 24.x
- **UI 组件**: Element Plus

## 测试策略

### 测试金字塔

```
        /\
       /  \       E2E 测试 (Docker Compose)
      /____\
     /      \     集成测试 (API 集成)
    /________\
   /          \   单元测试 (组件/服务)
  /____________\
```

### 后端测试覆盖

| 模块 | 测试类 | 用例数 | 覆盖范围 |
|------|--------|--------|----------|
| 登录鉴权 | `AuthControllerTest` | 14 | 登录成功/失败、登出、Token 管理 |
| 学生请假 | `StudentControllerTest` | 15 | 课程列表、请假申请、请假记录 |
| 辅导员审核 | `CounselorControllerTest` | 15 | 待审核列表、审核通过/拒绝、统计 |
| 教师统计 | `TeacherControllerTest` | 10 | 课程列表、请假查询、出勤统计 |
| 权限拦截 | `AuthInterceptorTest` | 14 | Token 验证、角色权限、跨角色访问 |
| 异常处理 | `GlobalExceptionHandlerTest` | 16 | 参数验证、业务异常、系统异常 |

### 前端测试覆盖

| 模块 | 测试文件 | 用例数 | 覆盖范围 |
|------|----------|--------|----------|
| 登录页面 | `Login.spec.js` | 13 | 表单渲染、登录流程、错误提示 |
| 路由守卫 | `index.spec.js` | 11 | 认证拦截、角色路由、重定向 |
| 请求封装 | `request.spec.js` | 18 | 拦截器、错误处理、Token 管理 |
| 学生课程 | `CourseList.spec.js` | 9 | 课程列表、跳转请假、空状态 |
| 辅导员审核 | `PendingList.spec.js` | 10 | 待审核列表、审核操作、分页 |
| 教师统计 | `AttendanceStats.spec.js` | 10 | 统计查询、图表渲染、筛选 |

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.8+
- Node.js 20+
- Docker & Docker Compose (可选，用于容器化测试)

### 一键运行所有测试

```bash
# 使用 Makefile
make test

# 或手动运行
cd backend && mvn test -Dspring.profiles.active=test
cd ../frontend && npm install && npm run test
```

## 后端测试

### 测试配置

测试使用独立的 `application-test.yml` 配置文件，主要配置：

```yaml
spring:
  profiles:
    active: test
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  sql:
    init:
      schema-locations: classpath:db/schema.sql
      data-locations: classpath:db/data.sql
```

### 运行后端测试

```bash
# 运行所有测试
cd backend
mvn test -Dspring.profiles.active=test

# 运行特定测试类
mvn test -Dtest=AuthControllerTest

# 运行特定测试方法
mvn test -Dtest=AuthControllerTest#testStudentLoginSuccess

# 生成测试报告
mvn surefire-report:report
```

### 测试基类

所有后端测试都继承自 `BaseTest`，提供通用功能：

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseTest {
    // 提供 loginAndGetToken()、getAuthHeader() 等工具方法
}
```

### 测试数据

- `src/test/resources/db/schema.sql` - H2 数据库表结构
- `src/test/resources/db/data.sql` - 测试初始化数据

测试账号：
- 学生: `student1` / `123456`
- 教师: `teacher1` / `123456`
- 辅导员: `counselor1` / `123456`

## 前端测试

### 测试配置

```javascript
// vitest.config.js
export default defineConfig({
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.js',
    include: ['**/*.spec.js'],
    coverage: {
      reporter: ['text', 'json', 'html']
    }
  }
})
```

### 运行前端测试

```bash
# 运行所有测试
cd frontend
npm install
npm run test

# 监听模式（开发时使用）
npm run test:watch

# 可视化测试界面
npm run test:ui

# 生成覆盖率报告
npm run test:coverage
```

### 测试工具

`src/test/utils.js` 提供测试工具函数：

```javascript
createTestRouter()      // 创建测试用路由实例
mockAxios()            // Mock Axios 请求
flushPromises()        // 等待所有 Promise 解决
```

### Mock 浏览器 API

在 `src/test/setup.js` 中已预置以下 Mock：

- `matchMedia`
- `ResizeObserver`
- `IntersectionObserver`
- `localStorage`
- Element Plus 组件

## Docker 测试

### 一键运行 Docker 测试

```bash
# 运行所有测试（前后端）
docker compose -f docker-compose.test.yml up --build --abort-on-container-exit

# 或使用 Makefile
make test-docker

# 仅运行后端测试
make test-docker-backend

# 仅运行前端测试
make test-docker-frontend
```

### Docker 测试架构

```
┌─────────────────────────────────────────────────────┐
│                     test-network                    │
│  ┌──────────────┐          ┌──────────────────┐    │
│  │ backend-test │   ────>  │  frontend-test   │    │
│  │  (JUnit 5)   │   依赖    │   (Vitest)      │    │
│  │  H2 数据库    │          │  jsdom 环境      │    │
│  └──────────────┘          └──────────────────┘    │
│         │                            │             │
│         ▼                            ▼             │
│  surefire-reports              test-results        │
│  site (jacoco)                  coverage           │
└─────────────────────────────────────────────────────┘
```

### 测试报告输出

Docker 测试完成后，报告将输出到本地目录：

```
backend/
├── target/
│   ├── surefire-reports/    # JUnit XML 报告
│   └── site/                # HTML 报告

frontend/
├── test-results/            # Vitest XML 报告
└── coverage/                # 覆盖率报告
```

### Dockerfile 说明

#### 后端测试 Dockerfile (`backend/Dockerfile.test`)

```dockerfile
FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn test -Dspring.profiles.active=test
```

#### 前端测试 Dockerfile (`frontend/Dockerfile.test`)

```dockerfile
FROM node:20.10.0-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run test
```

## 测试报告

### 后端测试报告

```bash
# 生成 Surefire 报告
cd backend
mvn surefire-report:report

# 报告位置
target/site/surefire-report.html
```

### 前端测试报告

```bash
# 生成覆盖率报告
cd frontend
npm run test:coverage

# 报告位置
coverage/index.html
```

### 测试覆盖率目标

| 类型 | 目标覆盖率 | 当前状态 |
|------|-----------|----------|
| 后端控制器 | >= 80% | 已覆盖核心模块 |
| 后端服务层 | >= 70% | - |
| 前端组件 | >= 60% | 已覆盖核心页面 |
| 前端工具函数 | >= 80% | 已覆盖 request/router |

## 常见问题

### 1. H2 数据库兼容性问题

**问题**: MySQL 语法在 H2 中不兼容
**解决方案**: 使用 H2 兼容的 SQL 语法，如用 `IDENTITY` 替代 `AUTO_INCREMENT`

### 2. Element Plus 组件测试报错

**问题**: `ResizeObserver` or `matchMedia` is not defined
**解决方案**: 在 `setup.js` 中已预置 Mock，确保配置正确

### 3. Docker 测试网络问题

**问题**: 服务间无法通信
**解决方案**: 确保使用 `docker-compose.test.yml` 中定义的网络 `test-network`

### 4. 测试数据隔离

**问题**: 测试间数据相互影响
**解决方案**: 每个测试类使用独立的 H2 数据库实例，`@Transactional` 自动回滚

### 5. 前端测试异步问题

**问题**: 组件更新后断言失败
**解决方案**: 使用 `await nextTick()` 或 `flushPromises()` 等待 DOM 更新

## 最佳实践

1. **测试命名**: 使用 `should_xxx_when_xxx` 格式命名测试方法
2. **数据驱动**: 使用 `@ParameterizedTest` 进行多场景测试
3. **Mock 外部依赖**: 单元测试只测试当前单元，外部依赖全部 Mock
4. **测试独立性**: 每个测试用例独立运行，不依赖其他测试
5. **覆盖率监控**: 定期检查测试覆盖率，确保核心逻辑被覆盖
6. **CI/CD 集成**: 将 Docker 测试集成到 CI/CD 流水线

## 扩展阅读

- [JUnit 5 官方文档](https://junit.org/junit5/docs/current/user-guide/)
- [Spring Boot Test 文档](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Vitest 官方文档](https://vitest.dev/)
- [Vue Test Utils 文档](https://test-utils.vuejs.org/)
- [Docker Compose 文档](https://docs.docker.com/compose/)
