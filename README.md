# 💪 被拒之后

> **被拒不是终点，只是开始**

面向求职被拒者的 AI 激励互助社区 —— 用户发帖倾诉，三个 AI 从不同视角安慰你，情绪越重，来的 AI 越多。

## ✨ 项目亮点

- 🤖 **三 AI 串行安慰**：DeepSeek（心情）、Kimi2（历史）、Qwen（技术），情绪分越高触发越多
- 🔄 **轮询实时更新**：发帖后前端每 3 秒轮询，AI 回复逐条渲染，无需维护长连接
- 🔐 **JWT + 角色权限**：用户/管理员双角色，拦截器级权限校验
- 🛡️ **内容安全**：关键词过滤 + 管理员审核双保险
- 🌙 **暗色主题**：Tailwind CSS 深色 UI，温暖粉色调
- 📱 **响应式设计**：移动端友好，纯 SPA 架构

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────────────┐
│              Vue 3 + Vite + Tailwind CSS             │
│     首页 Feed │ 帖子详情 │ 发帖 │ 登录注册 │ 管理后台   │
└────────────────────────┬────────────────────────────┘
                         │ REST API (Axios)
┌────────────────────────▼────────────────────────────┐
│              Spring Boot 3.4 + MyBatis-Plus          │
│  ┌──────────┐  ┌──────────┐  ┌────────────────────┐ │
│  │ 用户模块  │  │ 帖子模块  │  │   AI 调度模块      │ │
│  │ 注册/登录 │  │ 发帖/点赞 │  │ (Spring AI 1.0)   │ │
│  │ JWT 认证  │  │ 评论回复  │  │ 串行调用三模型     │ │
│  └──────────┘  └──────────┘  └────────────────────┘ │
│  ┌──────────┐  ┌──────────┐                          │
│  │ 管理模块  │  │ 审核模块  │                          │
│  │ 统计/用户 │  │ 关键词过滤│                          │
│  └──────────┘  └──────────┘                          │
└────────────────────────┬────────────────────────────┘
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
   ┌──────────┐   ┌────────────┐   ┌──────────┐
   │  MySQL 8 │   │ OpenRouter │   │ SMTP 邮件 │
   │  数据存储  │   │ AI 统一网关  │   │  验证码   │
   └──────────┘   └────────────┘   └──────────┘
```

## 🛠️ 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.4.0 | Web 框架 |
| Spring AI | 1.0.0 | AI 模型集成 |
| MyBatis-Plus | 3.5.10 | ORM 框架 |
| MySQL | 8.0+ | 数据库 |
| JJWT | 0.12.6 | JWT 认证 |
| BCrypt | — | 密码加密 |
| Hutool | 5.8.29 | 工具库 |
| Lombok | — | 代码简化 |
| Maven | 3.9+ | 构建工具 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4+ | UI 框架 |
| Vite | 5.4+ | 构建工具 |
| Pinia | 2.1+ | 状态管理 |
| Vue Router | 4.3+ | 路由 |
| Axios | 1.7+ | HTTP 客户端 |
| Tailwind CSS | 3.4+ | 样式框架 |

### AI 模型（通过 OpenRouter 统一接入）

| AI | 模型 | 视角 | 触发条件 |
|----|------|------|---------|
| DeepSeek | deepseek/deepseek-v3.2 | 💙 心情安慰 | 每帖必回 |
| Kimi2 | moonshotai/kimi-k2.6 | 🧠 历史典故 | 情绪分 ≥ 2 |
| Qwen | qwen/qwen3.6-plus | 🌟 技术方向 | 情绪分 ≥ 2 |

## 📁 项目结构

```
beijvzhihou-backend/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/beijvzhihou/
│   │   ├── common/                   # 通用模块
│   │   │   ├── Result.java           # 统一响应封装
│   │   │   ├── ResultCode.java       # 状态码枚举
│   │   │   ├── BusinessException.java # 业务异常
│   │   │   └── GlobalExceptionHandler.java # 全局异常处理
│   │   ├── config/                   # 配置类
│   │   │   ├── AiProperties.java     # AI 配置属性
│   │   │   ├── SpringAiConfig.java   # Spring AI 多模型配置
│   │   │   ├── WebMvcConfig.java     # MVC 配置（拦截器注册）
│   │   │   ├── WebConfig.java        # CORS 配置
│   │   │   ├── MybatisPlusConfig.java # 分页插件
│   │   │   └── AsyncConfig.java      # 异步线程池
│   │   ├── controller/               # 控制器
│   │   │   ├── AuthController.java   # 认证接口
│   │   │   ├── PostController.java   # 帖子接口
│   │   │   ├── CommentController.java # 评论接口
│   │   │   └── AdminController.java  # 管理接口
│   │   ├── dto/                      # 数据传输对象
│   │   ├── entity/                   # 数据库实体
│   │   ├── filter/                   # 过滤器
│   │   │   └── JwtAuthenticationFilter.java # JWT 解析
│   │   ├── interceptor/              # 拦截器
│   │   │   ├── AuthInterceptor.java  # 登录校验
│   │   │   └── AdminInterceptor.java # 管理员权限校验
│   │   ├── mapper/                   # MyBatis Mapper
│   │   ├── service/                  # 业务逻辑
│   │   │   ├── impl/
│   │   │   │   ├── AIServiceImpl.java       # AI 调用 + Prompt
│   │   │   │   ├── AiStreamServiceImpl.java # AI 串行调度
│   │   │   │   ├── AuthServiceImpl.java     # 注册/登录
│   │   │   │   ├── PostServiceImpl.java     # 帖子 CRUD
│   │   │   │   ├── CommentServiceImpl.java  # 评论
│   │   │   │   └── ReviewServiceImpl.java   # 内容审核
│   │   │   └── ...                   # Service 接口
│   │   ├── util/                     # 工具类
│   │   │   ├── JwtUtil.java          # JWT 工具
│   │   │   └── KeywordFilter.java    # 关键词过滤
│   │   └── BeijvzhihouApplication.java # 启动类
│   ├── src/main/resources/
│   │   ├── application.yml           # 主配置
│   │   ├── application-dev.yml       # 开发环境配置
│   │   └── keywords.txt              # 敏感词库
│   └── pom.xml
├── frontend/                         # Vue 3 前端
│   ├── src/
│   │   ├── api/index.js              # API 封装 + Axios 拦截
│   │   ├── stores/user.js            # Pinia 用户状态
│   │   ├── router/index.js           # 路由配置
│   │   ├── views/                    # 页面组件
│   │   │   ├── HomeView.vue          # 首页（发帖 + Feed）
│   │   │   ├── PostDetailView.vue    # 帖子详情（AI 回复 + 评论）
│   │   │   ├── LoginView.vue         # 登录
│   │   │   ├── RegisterView.vue      # 注册
│   │   │   ├── ProfileView.vue       # 个人中心
│   │   │   └── AdminView.vue         # 管理后台
│   │   ├── components/               # 公共组件
│   │   │   ├── NavBar.vue            # 导航栏
│   │   │   ├── PostCard.vue          # 帖子卡片
│   │   │   └── AiReplyCard.vue       # AI 回复卡片
│   │   ├── utils/format.js           # 格式化工具
│   │   └── assets/main.css           # 全局样式
│   ├── vite.config.js
│   ├── tailwind.config.js
│   └── package.json
└── docs/                             # 设计文档
```

## 🚀 快速开始

### 环境要求

- **JDK 17+**
- **Maven 3.9+**
- **Node.js 18+**
- **MySQL 8.0+**
- **OpenRouter API Key**（[获取地址](https://openrouter.ai/)）

### 1. 克隆项目

```bash
git clone https://github.com/tianbaoxing/beijvzhihou.git
cd beijvzhihou-backend
```

### 2. 初始化数据库

创建 MySQL 数据库并执行建表语句：

```sql
CREATE DATABASE IF NOT EXISTS beijvzhihou
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE beijvzhihou;

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `email` VARCHAR(255) NOT NULL UNIQUE,
  `email_masked` VARCHAR(255) NOT NULL,
  `nickname` VARCHAR(50) NOT NULL,
  `avatar_url` VARCHAR(500) DEFAULT '',
  `password_hash` VARCHAR(255) DEFAULT '',
  `status` TINYINT DEFAULT 1,
  `role` VARCHAR(20) DEFAULT 'USER',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `post` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `content` TEXT NOT NULL,
  `emotion_score_avg` DECIMAL(5,2) DEFAULT 0,
  `ai_response_count` INT DEFAULT 0,
  `comment_count` INT DEFAULT 0,
  `like_count` INT DEFAULT 0,
  `view_count` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_reply` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `post_id` BIGINT NOT NULL,
  `ai_type` VARCHAR(20) NOT NULL,
  `reply_content` TEXT NOT NULL,
  `trigger_score` TINYINT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `post_like` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `post_id` BIGINT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
  INDEX `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `email_code` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `email` VARCHAR(255) NOT NULL,
  `code` VARCHAR(10) NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `expire_time` DATETIME NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_email_type` (`email`, `type`),
  INDEX `idx_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `content_review` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `post_id` BIGINT NOT NULL,
  `review_type` VARCHAR(20) NOT NULL,
  `result` VARCHAR(20) NOT NULL,
  `reason` VARCHAR(500) DEFAULT '',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `comment` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `content` TEXT NOT NULL,
  `parent_id` BIGINT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_post_id` (`post_id`),
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 3. 配置后端

编辑 `backend/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/beijvzhihou?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: your_mysql_username
    password: your_mysql_password

  mail:
    host: smtp.qq.com
    port: 587
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}

app:
  jwt:
    secret: ${JWT_SECRET:your-secret-key-at-least-32-chars}
  ai:
    emotion-threshold: 2
    deepseek:
      api-key: ${OPENROUTER_API_KEY:}
      base-url: https://openrouter.ai/api/v1
      model: deepseek/deepseek-v3.2
    kimi2:
      api-key: ${OPENROUTER_API_KEY:}
      base-url: https://openrouter.ai/api/v1
      model: moonshotai/kimi-k2.6
    qwen:
      api-key: ${OPENROUTER_API_KEY:}
      base-url: https://openrouter.ai/api/v1
      model: qwen/qwen3.6-plus
```

或通过环境变量配置（推荐生产环境）：

```bash
export OPENROUTER_API_KEY=sk-or-v1-xxxxx
export JWT_SECRET=your-production-secret-key
export MAIL_USERNAME=your-email@qq.com
export MAIL_PASSWORD=your-smtp-password
```

### 4. 启动后端

```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:5173`，自动代理 `/api` 请求到后端

### 6. 设置管理员

注册账号后，在数据库中将用户角色设为管理员：

```sql
UPDATE `user` SET `role` = 'ADMIN' WHERE `email` = 'your-email@example.com';
```

## 📡 API 概览

### 认证模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/send-code` | 发送邮箱验证码 | ❌ |
| POST | `/api/auth/register` | 注册 | ❌ |
| POST | `/api/auth/login` | 登录 | ❌ |
| GET | `/api/auth/me` | 获取当前用户 | ✅ |
| PUT | `/api/auth/me` | 更新个人资料 | ✅ |

### 帖子模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/posts` | 帖子列表（分页/排序） | ❌ |
| GET | `/api/posts/{id}` | 帖子详情 | ❌ |
| POST | `/api/posts` | 发帖 | ✅ |
| POST | `/api/posts/{id}/like` | 点赞/取消点赞 | ❌（支持匿名） |

### 评论模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/posts/{postId}/comments` | 评论列表 | ❌ |
| POST | `/api/posts/{postId}/comments` | 发表评论 | ✅ |

### 管理模块（需 ADMIN 角色）

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/admin/stats` | 统计数据 | 🔒 ADMIN |
| GET | `/api/admin/users` | 用户列表 | 🔒 ADMIN |
| GET | `/api/admin/posts` | 帖子列表 | 🔒 ADMIN |
| GET | `/api/admin/review/pending` | 待审核帖子 | 🔒 ADMIN |
| POST | `/api/admin/review/{id}` | 审核帖子 | 🔒 ADMIN |

## 🤖 AI 工作流程

```
用户发帖 → 后端保存帖子 → 启动异步线程
                              │
                              ▼
                    [Step 1] DeepSeek（必回）
                    💙 心情安慰视角
                    评估情绪分 1-10
                              │
                              ▼
                    情绪分 ≥ 2 ?
                    ┌──── 是 ────┐
                    ▼            ▼
              [Step 2] Kimi2   [Step 3] Qwen
              🧠 历史典故视角   🌟 技术方向视角
                    │            │
                    └─────┬──────┘
                          ▼
                  结果逐条写入 ai_reply 表
                          │
                          ▼
                  前端轮询 GET /api/posts/{id}
                  每 3 秒检测新 AI 回复
                  动态渲染到页面
```

**关键设计：**

- AI 调用失败时自动降级为模拟回复，确保用户体验不中断
- 情绪分阈值 `app.ai.emotion-threshold` 可配置，默认 2
- 前端轮询超时上限 60 秒，超时后加载动画消失
- AI 回复包含情绪分可视化（1-10 圆点指示器）

## 🔐 安全设计

| 层级 | 机制 | 说明 |
|------|------|------|
| 认证 | JWT + Bearer Token | 过滤器解析 Token，拦截器校验登录 |
| 密码 | BCrypt 加密 | Spring Security Crypto |
| 权限 | 角色拦截器 | AdminInterceptor 校验 ADMIN 角色 |
| 内容 | 关键词过滤 | keywords.txt 正则匹配 |
| 审核 | 管理员审核 | 帖子状态：1=已发布 2=待审核 0=已拒绝 |
| 邮箱 | 脱敏展示 | c\*\*\*a@qq.com |
| CORS | 白名单 | 仅允许 localhost 开发端口 |

## 🎨 前端页面

| 页面 | 路径 | 功能 |
|------|------|------|
| 首页 | `/` | 帖子 Feed + 发帖入口 |
| 帖子详情 | `/post/:id` | AI 回复 + 用户评论 + 点赞 |
| 登录 | `/login` | 邮箱 + 密码登录 |
| 注册 | `/register` | 邮箱验证码注册 |
| 个人中心 | `/profile` | 修改昵称 |
| 管理后台 | `/admin` | 统计/用户/帖子/审核（仅 ADMIN） |

## 🧪 测试

```bash
# 后端测试
cd backend
mvn test

# 单独运行单元测试
mvn test -Dtest="AdminInterceptorTest,AIServiceTest"

# 单独运行集成测试
mvn test -Dtest="ApplicationIntegrationTest"
```

## 📦 生产部署

### 后端打包

```bash
cd backend
mvn clean package -DskipTests
java -jar target/beijvzhihou-backend-1.0.0.jar \
  --spring.profiles.active=prod \
  --app.jwt.secret=${JWT_SECRET} \
  --app.ai.deepseek.api-key=${OPENROUTER_API_KEY}
```

### 前端构建

```bash
cd frontend
npm run build
# 产物在 dist/ 目录，部署到 Nginx 或 CDN
```

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /var/www/beijvzhihou/dist;
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 🗺️ 路线图

- [x] 用户注册/登录（邮箱验证码）
- [x] 帖子发布与 Feed 流
- [x] 三 AI 串行安慰机制
- [x] 轮询实时更新 AI 回复
- [x] 评论系统（支持嵌套回复）
- [x] 点赞（支持匿名浏览器指纹）
- [x] 管理员权限校验
- [x] 管理后台（统计/用户/帖子/审核）
- [x] 关键词过滤
- [x] 内容审核
- [ ] Redis 缓存替换内存存储
- [ ] 图片上传支持
- [ ] 通知系统
- [ ] 用户关注
- [ ] 深色/浅色主题切换

## 📄 开源协议

[MIT License](LICENSE)

---

<p align="center">
  被拒不是终点，只是开始 💪<br/>
  如果这个项目对你有帮助，请给个 ⭐ Star
</p>
