# 「被拒之后」项目设计方案

> 版本：V1.2  
> 日期：2026-05-15  
> 负责人：黑暗游侠  
> 状态：**已基本实现**（核心功能完成，部分待优化）

---

## 一、产品概述

| 项 | 内容 |
|---|------|
| 产品名 | **被拒之后** |
| 定位 | 面向多次求职被拒者的 AI 激励互助社区 |
| 核心价值 | 用户发帖倾诉 → 三个 AI 从不同视角安慰 → 情绪越重，来的 AI 越多 |
| Slogan | 被拒不是终点，只是开始 |
| 合规说明 | 未注册用户可浏览+点赞；发帖/评论需注册（中国互联网法规合规） |

---

## 二、技术架构

### 2.1 技术栈

| 层级 | 技术选型 | 版本要求 |
|------|---------|---------|
| 前端框架 | Vue 3 + Vite | Vue 3.5+ / Vite 6+ |
| 前端 UI | Element Plus / Naive UI | 最新稳定版 |
| 后端框架 | Spring Boot 3 | 3.4.x |
| AI 集成 | Spring AI 1.0 + OpenRouter | OpenAI 兼容格式 |
| 数据库 | MySQL 8 / PostgreSQL | 8.0+ |
| 缓存 | ConcurrentHashMap | - |
| 构建工具 | Maven | 3.9+ |

### 2.2 系统架构图

```
┌──────────────────────────────────────────────────────────────┐
│                    Vue 3 SPA (前端)                           │
│          首页 Feed │ 帖子详情 │ 发帖 │ 登录注册 │ 个人中心        │
└────────────────────────┬───────────────────────────────────┘
                         │ HTTP / REST API
┌────────────────────────▼───────────────────────────────────┐
│                  Spring Boot 3 (后端)                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │  用户模块    │  │  帖子模块    │  │    AI 调度模块       │ │
│  │   注册/登录   │  │   发帖/点赞   │  │  (Spring AI Client) │ │
│  │              │  │   评论回复    │  │                     │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
│                                                              │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              OpenRouter（统一接口）                      │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐  │ │
│  │  │ deepseek/     │  │ moonshotai/  │  │ qwen/       │  │ │
│  │  │ deepseek-v3.2 │  │ kimi-k2.6    │  │ qwen3.6-plus│  │ │
│  │  │ (心情视角)     │  │ (历史视角)    │  │ (技术视角)   │  │ │
│  │  └──────────────┘  └──────────────┘  └─────────────┘  │ │
│  └─────────────────────────────────────────────────────────┘ │
└────────────────────────┬───────────────────────────────────┘
                         │
              ┌──────────▼──────────┐
              │     MySQL     │   ConcurrentHashMap   │
              │     存储      │      内存缓存       │
              └─────────┴─────────────┘
```

### 2.3 运行环境（临时 PATH 方案）

> **⚠️ ⚠️ ⚠️ 重要警告 ⚠️ ⚠️ ⚠️**
> 
> **必须在每次打开新终端时执行以下命令！否则会出现 Maven 版本不兼容错误！**
> 
> **常见错误：** `The plugin org.apache.maven.plugins:maven-compiler-plugin:3.13.0 requires Maven version 3.6.3`
> 
> **原因：** 系统默认 Maven 版本过低（如 3.6.x），与 Spring Boot 3.4.0 要求的 Maven 3.9.x 不兼容
> 
> **解决方案：** 必须使用项目指定的 JDK 17 + Maven 3.9.12

```powershell
# ============================================
# 【强制执行】每次开终端必须运行（不修改全局环境变量）
# ============================================
$env:JAVA_HOME = "E:\ai\jdk-17.0.2"
$env:Path = "E:\ai\jdk-17.0.2\bin;E:\ai\node-v20.19.4-win-x64;E:\ai\apache-maven-3.9.12\bin;" + $env:Path

# 验证环境（必须显示以下版本）
java -version  # 必须显示 17.x (如 openjdk version "17.0.2")
node --version  # 必须显示 20.x
mvn -version    # 必须显示 3.9.x (如 Apache Maven 3.9.12)

# 如果显示的不是上述版本，编译会失败！！！
```

> **✅ 验证成功标志：**
> - Java version: 17.x
> - Maven version: 3.9.x
> - Node.js version: 20.x
> 
> **❌ 失败原因排查：**
> 1. 如果 Maven 版本显示为 3.6.x 或更低 → PATH 中旧版本优先
> 2. 如果 Java 版本显示为 1.8.x → JAVA_HOME 未正确设置
> 3. 确保命令执行后再运行 `mvn compile` 或 `mvn spring-boot:run`

---

## 三、AI 触发流程

### 3.1 三 AI 角色定位

| AI | 模型 | 视角 | 触发方式 |
|----|------|------|---------|
| AI#1 | DeepSeek | 心情安慰 | **默认必回**，每帖必触 |
| AI#2 | Kimi2（Moonshot） | 历史典故 | 情绪分 ≥ 2 时触发 |
| AI#3 | Qwen（通义千问） | 技术方向 | 情绪分 ≥ 2 时触发 |

### 3.1.1 AI 模型接入配置（OpenRouter）

> 三个 AI 模型统一通过 [OpenRouter](https://openrouter.ai/) 接入，使用 OpenAI 兼容接口（`/chat/completions`）。

| AI | OpenRouter 模型标识 | 视角 | 触发方式 |
|----|---------------------|------|---------|
| AI#1 | `deepseek/deepseek-v3.2` | 心情安慰 | **默认必回**，每帖必触 |
| AI#2 | `moonshotai/kimi-k2.6` | 历史典故 | 情绪分 ≥ 2 时触发 |
| AI#3 | `qwen/qwen3.6-plus` | 技术方向 | 情绪分 ≥ 2 时触发 |

**配置参数：**

```yaml
app:
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

**接入说明：**

- 使用 **Spring AI 1.0** 框架（`spring-ai-starter-model-openai`）接入 OpenRouter
- 三个模型共用同一个 OpenRouter API Key（环境变量 `OPENROUTER_API_KEY`）
- 统一使用 OpenAI 兼容格式，`completionsPath` 设为 `/chat/completions`（避免与 base-url 中的 `/v1` 重复）
- 后端 `SpringAiConfig` 配置类手动创建三个 `OpenAiChatModel` Bean（`deepseekChatModel`、`kimi2ChatModel`、`qwenChatModel`）
- `AIServiceImpl` 通过 `@Qualifier` 注入对应的 `ChatModel`，使用 `Prompt` + `SystemMessage` + `UserMessage` 调用
- 排除了 Spring AI 的自动配置（`OpenAiChatAutoConfiguration` 等），避免默认 ChatModel 创建时要求 `spring.ai.openai.api-key`
- API 调用失败时自动降级为模拟回复，确保用户体验不中断

### 3.2 情绪分计算

每个 AI 独立判断自己的情绪阈值：
- 读取帖子内容后，AI 自行评估情绪强度（1-10分）
- ≥ 2 分 → 回复；< 2 分 → 跳过
- 情绪分存入 `ai_reply.emotion_score` 字段

### 3.3 触发时序图

```
用户发帖 ──→ 跳转帖子详情页（显示加载动画）
                │
                ▼
         [Step 1] DeepSeek（必回，心情视角）
             │ 后台完成 → 前端轮询刷新 → 页面渲染 DeepSeek 回复
             ▼
         [Step 2] Kimi2（阈值判断，情绪分≥2）
             │ 后台完成 → 前端轮询刷新 → 页面渲染 Kimi2 回复
             ▼
         [Step 3] Qwen（阈值判断，情绪分≥2）
             │ 后台完成 → 前端轮询刷新 → 页面渲染 Qwen 回复
             ▼
         前端轮询检测到 AI 回复数不再增加 → 加载动画消失
```

**技术实现：**

- `POST /api/posts` 调用 `createPostAndStartAiStream()` → 立即返回帖子信息（不等 AI）
- 后台启动新线程，通过 `AiStreamService` 串行调用三个 AI，结果逐条写入 `ai_reply` 表
- 前端跳转帖子详情页后，使用 **轮询**（每 3 秒调用 `GET /api/posts/{id}`）检测 AI 回复：
  - 首次请求无 AI 回复 → 显示加载动画
  - 轮询到新回复 → 动态渲染到页面
  - 连续多次轮询 AI 回复数不再增加 → 加载动画消失（超时上限 60 秒）
- 轮询方案优势：实现简单、兼容性好、无需维护 SSE 长连接

### 3.4 AI Prompt 示例

#### AI#1 - DeepSeek（心情安慰）
```
你是一个温暖的朋友。用户刚刚收到了求职被拒的消息。
请从情感共鸣的角度，给予真诚的安慰和陪伴。
不要说教，不要给建议，只是陪伴和认可对方的感受。
语气温暖，像一个真正理解他的朋友。
用户发帖内容如下：
{content}
```

#### AI#2 - Kimi2（历史典故）
```
你是一个博学的好友。请阅读下面的求职者的倾诉帖子。
评估他的情绪强度（1-10分），如果≥2分，请举1-2个历史上或名人中求职/人生被拒后最终成功的真实例子。
语气真诚，既不空洞鼓励，也要让对方感受到"被理解"。
帖子内容：
{content}
```

#### AI#3 - Qwen（技术方向）
```
你是一个资深的技术面试官和职业顾问。请阅读下面的帖子。
评估发帖人的情绪强度（1-10分），如果≥2分，请从简历优化、面试技巧、行业选择等角度给出1-2条实际可行的方向性建议。
语气专业但温和，既要实用，也不要让对方觉得你在说教。
帖子内容：
{content}
```

---

## 四、数据模型

### 4.1 ER 图

```
user ──1:N──> post ──1:N──> ai_reply
  │              │              │
  │              │              └── ai_provider / emotion_score / content
  │              │
  │              └──1:N──> comment（用户评论）
  │                           │
  │                           └── user_id / content / parent_id（支持楼中楼）
  │
  └──1:N──> post_like（浏览器指纹去重）
```

### 4.2 表结构

```sql
-- 用户表
CREATE TABLE user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    email           VARCHAR(255) NOT NULL UNIQUE,   -- AES-256 加密存储原文
    email_masked    VARCHAR(255) NOT NULL,          -- 脱敏显示: c***a@qq.com
    nickname        VARCHAR(50) NOT NULL,            -- 随机匿名昵称
    avatar_url      VARCHAR(500),                    -- 随机头像 URL
    password_hash   VARCHAR(255) NOT NULL,           -- BCrypt 加密
    status          TINYINT DEFAULT 1 COMMENT '1=正常 0=禁用',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 帖子表
CREATE TABLE post (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id             BIGINT NOT NULL,
    content             TEXT NOT NULL,
    emotion_score_avg   DECIMAL(3,1) DEFAULT 0,      -- AI 回复的平均情绪分
    ai_response_count   INT DEFAULT 0,               -- 已触发的 AI 回复数
    comment_count       INT DEFAULT 0,               -- 评论数（缓存）
    like_count          INT DEFAULT 0,               -- 点赞数（缓存）
    view_count          INT DEFAULT 0,               -- 浏览数
    status              TINYINT DEFAULT 1 COMMENT '1=正常 2=审核中 0=已删除',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- AI 回复表
CREATE TABLE ai_reply (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id         BIGINT NOT NULL,
    ai_provider     VARCHAR(50) NOT NULL COMMENT 'DeepSeek / Kimi2 / Qwen',
    perspective     VARCHAR(50) NOT NULL COMMENT '心情 / 历史 / 技术',
    emotion_score   DECIMAL(3,1),                    -- 该 AI 评估的情绪分
    content         TEXT NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES post(id)
);

-- 用户评论表（登录用户互相安慰）
CREATE TABLE comment (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id         BIGINT NOT NULL,                 -- 所属帖子
    user_id         BIGINT NOT NULL,                 -- 评论者（必须登录）
    content         TEXT NOT NULL,                    -- 评论内容（200字以内）
    parent_id       BIGINT DEFAULT NULL,             -- 父评论ID（NULL=顶级评论，非NULL=回复）
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES post(id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (parent_id) REFERENCES comment(id),
    INDEX idx_post_id (post_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 点赞表（支持未登录点赞：浏览器指纹去重）
CREATE TABLE post_like (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id     BIGINT NOT NULL,
    user_id     BIGINT COMMENT 'NULL=匿名点赞',
    ip_hash     VARCHAR(64) COMMENT '浏览器指纹（fingerprint），用于未登录点赞去重',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_ip (post_id, ip_hash),
    UNIQUE KEY uk_post_user (post_id, user_id),
    FOREIGN KEY (post_id) REFERENCES post(id)
);

-- 内容审核日志
CREATE TABLE content_review (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id       BIGINT,
    review_type   VARCHAR(20) COMMENT 'keyword / ai / manual',
    result        VARCHAR(20) COMMENT 'pass / reject',
    reason        VARCHAR(500),
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 邮件验证码表
CREATE TABLE email_code (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    email       VARCHAR(255) NOT NULL,
    code        VARCHAR(10) NOT NULL,
    type        VARCHAR(20) COMMENT 'register / login / reset',
    expires_at   DATETIME NOT NULL,
    used         TINYINT DEFAULT 0,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

---

## 五、API 设计

### 5.1 认证

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/auth/send-code` | POST | 发送邮箱验证码 |
| `/api/auth/register` | POST | 邮箱注册 |
| `/api/auth/login` | POST | 邮箱登录 |
| `/api/auth/logout` | POST | 登出 |

### 5.2 帖子

| 接口 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/posts` | GET | 帖子列表（分页+排序，每页10条） | 公开 |
| `/api/posts/:id` | GET | 帖子详情 + AI 回复列表 + 评论列表（支持 fingerprint 参数标识匿名点赞状态） | 公开 |
| `/api/posts` | POST | 发帖（异步触发 AI，立即返回） | 需登录 |
| `/api/posts/:id/like` | POST | 点赞/取消点赞（请求体传递 fingerprint 标识匿名用户） | 公开 |
| `/api/posts/:id/comments` | GET | 帖子评论列表（分页） | 公开 |
| `/api/posts/:id/comments` | POST | 发表评论 | 需登录 |

### 5.3 用户

| 接口 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/user/profile` | GET | 当前用户信息 | 需登录 |
| `/api/user/profile` | PUT | 更新昵称/头像 | 需登录 |
| `/api/user/posts` | GET | 我的帖子列表 | 需登录 |

### 5.4 管理

| 接口 | 方法 | 描述 | 权限 |
|------|------|------|------|
| `/api/admin/review/pending` | GET | 待审核帖子列表 | 管理员 |
| `/api/admin/review/:id` | POST | 审核操作 | 管理员 |
| `/api/admin/stats` | GET | 数据统计 | 管理员 |

### 5.5 评论请求/响应格式

**发表评论请求：**
```json
POST /api/posts/{id}/comments
Authorization: Bearer <token>
{
    "content": "抱抱你，被拒不是你的问题",
    "parentId": null
}
```

**评论列表响应：**
```json
GET /api/posts/{id}/comments?page=1&size=10
{
    "code": 200,
    "data": {
        "list": [
            {
                "id": 1,
                "postId": 1,
                "userId": 2,
                "nickname": "快乐的小鱼",
                "avatarUrl": "https://picsum.photos/seed/123",
                "content": "抱抱你，被拒不是你的问题",
                "parentId": null,
                "replies": [
                    {
                        "id": 2,
                        "postId": 1,
                        "userId": 1,
                        "nickname": "倔强的刺猬",
                        "avatarUrl": "https://picsum.photos/seed/456",
                        "content": "谢谢你的安慰",
                        "parentId": 1,
                        "createdAt": "2026-05-02T22:00:00"
                    }
                ],
                "createdAt": "2026-05-02T21:00:00"
            }
        ],
        "total": 1,
        "page": 1,
        "size": 10,
        "pages": 1
    }
}
```

---

## 六、页面规划

| 页面 | 路由 | 访问权限 | 核心功能 |
|------|------|---------|---------|
| 首页 Feed | `/` | 所有人 | 帖子列表、分页（每页10条）、排序（热度/最新）、无限滚动加载 |
| 帖子详情 | `/post/:id` | 所有人 | 帖子内容、AI 回复、用户评论、点赞 |
| 发帖页 | `/post/new` | 登录用户 | 文本输入、提交 |
| 注册页 | `/register` | 访客 | 邮箱注册流程 |
| 登录页 | `/login` | 访客 | 邮箱+密码登录 |
| 个人中心 | `/profile` | 登录用户 | 我的帖子、修改昵称 |
| 管理后台 | `/admin` | 管理员 | 内容审核、数据统计 |

### 6.1 浏览器交互规则

> 以下规则定义了前端页面与后端 API 的交互约束，是浏览器端测试用例的核心验收标准。

| 规则编号 | 规则描述 | 涉及页面 / API | 验收条件 |
|---------|---------|---------------|---------|
| BR-1 | 未登录用户只能浏览帖子，不能发帖、不能评论 | 首页 `/`、帖子详情 `/post/:id` | 未登录时发帖按钮隐藏或点击后跳转登录页；评论输入框不可见 |
| BR-2 | 未登录用户可以点赞 | 帖子详情 `/post/:id` → `POST /api/posts/:id/like` | 点赞按钮可见且可点击；后端以浏览器指纹（fingerprint）作为匿名标识 |
| BR-3 | 未登录点赞去重：用浏览器指纹（fingerprint）作为唯一标识，同一浏览器对同一帖子多次点击只记录一次；不同浏览器点赞累加 | `POST /api/posts/:id/like` | 第二次点击取消点赞（toggle）；前端在 localStorage 生成唯一指纹 UUID，请求时传递 fingerprint 参数；不同浏览器的指纹不同，各自点赞独立累加 |
| BR-4 | 登录后才能发帖和评论 | 发帖页 `/post/new`、帖子详情评论区 | 未登录访问 `/post/new` 重定向到 `/login`；登录后恢复 |
| BR-5 | 未登录点击发帖 → 跳转登录页 → 登录成功后回到发帖页 | 发帖页 `/post/new` → 登录页 `/login` | URL 携带 `redirect` 参数：`/login?redirect=/post/new`，登录成功后自动跳转 |
| BR-6 | 注册完毕后跳转到登录页 | 注册页 `/register` → 登录页 `/login` | 注册成功后自动跳转 `/login`，不自动登录 |
| BR-7 | 浏览器默认打开首页，首页默认展示最新帖子 | 首页 `/` | 默认排序为 `time`（最新）；调用 `GET /api/posts?sort=time&page=1&size=10` |
| BR-8 | 注册时邮箱格式校验 | 注册页 `/register` | 输入不合法邮箱（如 `123`、`123123@`、`1123123@qq.`）时提示"邮箱格式不正确" |
| BR-9 | 注册时验证码错误提示 | 注册页 `/register` | 输入错误验证码（如 `123`、`1234`、`123456`）时提示"验证码错误" |
| BR-10 | 注册时邮箱唯一性校验 | 注册页 `/register` → `POST /api/auth/register` | 已注册邮箱再次注册时提示"该邮箱已注册" |
| BR-11 | 注册时密码校验 | 注册页 `/register` | 密码长度必须 ≥ 8 位，必须包含数字和字母；不符合时提示具体原因 |
| BR-12 | 登录使用邮箱+密码 | 登录页 `/login` | 登录表单为邮箱+密码，不再使用验证码登录 |
| BR-13 | 登录成功后默认跳转首页 | 登录页 `/login` | 无 redirect 参数时登录成功跳转 `/`（首页） |
| BR-14 | 发帖后自动跳转到帖子详情页，页面通过轮询实时获取 AI 回复 | 发帖成功 → 跳转 `/post/:id` → 页面每 3 秒轮询 `GET /api/posts/{id}` → 检测到新 AI 回复 → 动态渲染 → AI 回复数不再增加 → 加载动画消失（超时上限 60 秒） |
| BR-15 | 登录用户可以对他人帖子发表评论 | 帖子详情 `/post/:id` → `POST /api/posts/:id/comments` | 登录用户在帖子详情页可见评论输入框，提交后评论实时显示在评论列表；未登录用户评论输入框不可见 |
| BR-16 | 帖子列表支持按最新/最热排序，每页10条，向下滚动自动加载下一页 | 首页 `/` → `GET /api/posts?sort=time&page=1&size=10` | 默认按最新排序（sort=time）；可切换为最热排序（sort=hot）；每页展示10条；向下滑动到底部自动请求下一页（page+1）；新数据追加到列表末尾 |

### 6.2 浏览器交互流程图

```
用户打开浏览器
    │
    ▼
[BR-7] 首页 / → 默认展示最新帖子（sort=time, size=10）
    │
    ├── 浏览帖子 ──→ [BR-1] 未登录可浏览，不可发帖/评论
    │
    ├── 排序切换 ──→ [BR-16] 最热（sort=hot）/ 最新（sort=time）
    │         └── 每页10条，向下滚动自动加载下一页
    │
    ├── 点赞 ──→ [BR-2] 未登录可点赞
    │         └── [BR-3] 同一浏览器同一帖子只记一次（浏览器指纹去重）；不同浏览器点赞累加
    │
    ├── 评论 ──→ [BR-15] 登录用户可对他人帖子评论
    │         └── [BR-1] 未登录评论输入框不可见
    │
    ├── 点击发帖 ──→ [BR-5] 未登录？→ 跳转 /login?redirect=/post/new
    │                          │
    │                          ▼ 已登录
    │                     发帖页 /post/new → [BR-4] 提交帖子
    │                          │
    │                          ▼
    │                     [BR-14] 发帖成功 → 跳转帖子详情页 `/post/:id`
    │                          │ 前端轮询 `GET /api/posts/{id}`（每 3 秒）
    │                          ├── 检测到 DeepSeek 回复 → 渲染
    │                          ├── 检测到 Kimi2 回复 → 渲染
    │                          ├── 检测到 Qwen 回复 → 渲染
    │                          └── AI 回复数不再增加 → 加载动画消失（超时 60 秒）
    │
    ├── 注册 ──→ [BR-8] 邮箱格式校验 → [BR-11] 密码校验（≥8位，数字+字母）
    │         └── [BR-9] 验证码错误提示
    │         └── [BR-10] 邮箱唯一性校验
    │         └── [BR-6] 注册成功 → 跳转 /login
    │
    └── 登录 ──→ [BR-12] 邮箱+密码登录
              └── [BR-13] 登录成功 → 跳转 redirect 参数指定页面（默认 /）
```

---

## 七、安全与隐私

### 7.1 邮箱处理

| 环节 | 处理方式 |
|------|---------|
| 存储 | AES-256 加密存储原文 |
| 展示 | 脱敏格式 `c***a@qq.com`（保留域名） |
| 查询 | 仅支持模糊匹配脱敏串 |

### 7.2 用户匿名化

- 注册后自动分配随机昵称（如「伤心的向日葵」「不服输的猫」）
- 自动分配随机头像（抽象图案/风景图，不含人脸）
- 帖子作者只显示匿名昵称，不暴露任何可识别信息

### 7.3 内容安全

- **关键词过滤**：发帖时实时关键词检测，命中直接拒绝
- **AI 异步审核**：发帖后异步调用 AI 判断内容安全性，违规自动下架
- **先发后审**：内容公开可见，审核在后台进行

### 7.4 防刷机制

| 防护项 | 方案 |
|--------|------|
| 注册防刷 | 同一 IP 60 秒内只能请求一次验证码 |
| 发帖防刷 | 登录用户 5 分钟最多发 1 篇 |
| 评论防刷 | 登录用户 30 秒内最多评论 1 次 |
| 点赞防刷 | 浏览器指纹去重，每个帖子每个浏览器只能赞 1 次；不同浏览器点赞累加 |
| AI 调用防刷 | ConcurrentHashMap 内存限流，每 IP 每分钟最多触发 5 次发帖 |

---

## 八、MVP 范围

### V1.0 包含

- ✅ 邮箱注册 + 验证码登录
- ✅ 随机匿名昵称 + 头像
- ✅ 发帖（文字，500字以内）
- ✅ 三 AI 安慰（DeepSeek 必回，Kimi2/Qwen 阈值触发）
- ✅ 公开 Feed（按时间/热度排序，每页10条，无限滚动加载）
- ✅ 点赞（支持匿名点赞）
- ✅ 帖子详情页 + AI 回复展示
- ✅ 用户评论区（登录用户互相安慰）
- ✅ 关键词内容审核
- ✅ AI 异步内容安全审核
- ✅ 管理员内容审核后台
- ✅ 基础数据统计

### 后续版本（V2.0+）

- ❌ 手机号注册（与邮箱并行）
- ❌ 求职进度追踪功能
- ❌ 简历分析 AI
- ❌ 个性化推荐算法
- ❌ 微信/钉钉通知推送
- ❌ 数据导出

---

## 九、项目结构

```
beijvzhihou/
├── backend/                      # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/beijvzhihou/
│   │       ├── BeijvzhihouApplication.java
│   │       ├── config/
│   │       │   ├── SecurityConfig.java
│   │       │   └── SpringAIConfig.java
│   │       ├── controller/
│   │       │   ├── AuthController.java
│   │       │   ├── PostController.java
│   │       │   ├── CommentController.java
│   │       │   └── AdminController.java
│   │       ├── service/
│   │       │   ├── AuthService.java
│   │       │   ├── PostService.java
│   │       │   ├── CommentService.java
│   │       │   ├── AIService.java
│   │       │   └── ReviewService.java
│   │       ├── mapper/
│   │       │   ├── CommentMapper.java
│   │       ├── entity/
│   │       │   ├── Comment.java
│   │       ├── dto/
│   │       │   ├── CommentCreateDTO.java
│   │       │   ├── CommentVO.java
│   │       ├── promopts/
│   │       ├── deepseek-prompt.txt
│   │       ├── kimi2-prompt.txt
│   │       └── qwen-prompt.txt
│   └── pom.xml
│
├── frontend/                    # Vue 3 前端
│   ├── src/
│   │   ├── views/
│   │   │   ├── Home.vue
│   │   │   ├── PostDetail.vue
│   │   │   ├── PostNew.vue
│   │   │   ├── Login.vue
│   │   │   ├── Register.vue
│   │   │   ├── Profile.vue
│   │   │   └── Admin.vue
│   │   ├── components/
│   │   ├── api/
│   │   ├── stores/
│   │   └── router/
│   ├── package.json
│   └── vite.config.ts
│
└── README.md
```

---

## 十、环境配置

### 10.1 后端 application.yml 示例

```yaml
spring:
  application:
    name: beijvzhihou
  datasource:
    url: jdbc:mysql://localhost:3306/beijvzhihou?useUnicode=true&characterEncoding=utf-8
    username: root
    password: ${DB_PASSWORD}
  ai:
    openai:
      api-key: ${DEEPSEEK_API_KEY}
      base-url: https://api.deepseek.com
    moonshot:
      api-key: ${KIMI2_API_KEY}
      base-url: https://api.moonshot.cn
    dashscope:
      api-key: ${QWEN_API_KEY}

server:
  port: 8080

app:
  jwt:
    secret: ${JWT_SECRET}
    expire-days: 7
  email:
    code-expire-minutes: 5
```

### 10.2 前端 .env 示例

```env
VITE_API_BASE=http://localhost:8080/api
```

---

## 十一、测试规范

### 11.1 测试框架

| 组件 | 技术选型 | 版本 |
|------|---------|------|
| 测试框架 | JUnit 5 | 内置于 Spring Boot |
| Mock 框架 | Mockito | 内置于 spring-boot-starter-test |
| 断言库 | AssertJ | 内置于 spring-boot-starter-test |
| 覆盖率 | JaCoCo | 0.8.11 |
| 测试数据库 | MySQL | 集成测试使用本地 MySQL（beijvzhihou_test 库） |

### 11.2 测试结构（按类型分层）

```
backend/src/test/java/com/beijvzhihou/
├── unit/                                    # 单元测试（纯 Mockito，无 Spring 上下文）
│   ├── service/
│   │   ├── AIServiceTest.java              # AI 服务测试
│   │   ├── AuthServiceTest.java            # 认证服务测试
│   │   ├── PostServiceTest.java            # 帖子服务测试
│   │   ├── CommentServiceTest.java         # 评论服务测试
│   │   └── ReviewServiceTest.java          # 审核服务测试
│   └── util/
│       ├── JwtUtilTest.java                # JWT 工具类测试
│       └── KeywordFilterTest.java          # 关键词过滤测试
│
├── web/                                     # Web 层测试（@WebMvcTest + MockMvc）
│   ├── AuthControllerTest.java             # 认证接口测试
│   ├── PostControllerTest.java             # 帖子接口测试
│   └── CommentControllerTest.java          # 评论接口测试
│
├── integration/                             # 集成测试（@SpringBootTest + MySQL + @Transactional）
│   ├── AuthIntegrationTest.java            # 认证全流程集成
│   ├── PostIntegrationTest.java            # 发帖→AI回复全流程集成
│   ├── CommentIntegrationTest.java         # 评论全流程集成
│   └── ApplicationIntegrationTest.java     # 应用启动集成测试
│
└── common/                                  # 公共测试工具
    ├── ResultTest.java                     # Result 响应封装测试
    └── TestDataBuilder.java                # 测试数据构建器
```

### 11.3 测试分层策略

| 层级 | 注解 | 依赖 | 速度 | 测试范围 |
|------|------|------|------|---------|
| **unit/** | `@ExtendWith(MockitoExtension.class)` | 纯 Mockito，无 Spring | ⚡ 毫秒级 | Service 业务逻辑 |
| **web/** | `@WebMvcTest` + `@Import({JwtAuthenticationFilter.class, JwtUtil.class})` | Spring MVC 上下文 + MockBean | 🔄 秒级 | Controller HTTP 请求/响应 |
| **integration/** | `@SpringBootTest` + `@Transactional` | 完整 Spring 上下文 + MySQL | 🐢 10秒+ | 端到端流程 |

### 11.4 测试用例覆盖

| 测试类 | 测试范围 | 层级 | 用例数 |
|--------|---------|------|--------|
| AIServiceTest | DeepSeek/Kimi2/Qwen 回复、生成回复组合 | unit | 4 |
| AuthServiceTest | 发送验证码、获取用户信息 | unit | 3 |
| PostServiceTest | 帖子详情 | unit | 1 |
| CommentServiceTest | 发表评论、评论列表 | unit | 3 |
| ReviewServiceTest | 关键词过滤、帖子审核 | unit | 5 |
| JwtUtilTest | Token 生成/解析/验证 | unit | 4 |
| KeywordFilterTest | 关键词检测/查找 | unit | 6 |
| AuthControllerTest | 发送验证码/注册/登录/获取用户 | web | 5 |
| PostControllerTest | 帖子列表/详情/发帖 | web | 4 |
| CommentControllerTest | 发表评论/评论列表 | web | 3 |
| AuthIntegrationTest | 认证全流程 | integration | 3 |
| PostIntegrationTest | 发帖→AI回复全流程 | integration | 4 |
| CommentIntegrationTest | 评论全流程 | integration | 3 |
| ApplicationIntegrationTest | 应用上下文/核心 Bean | integration | 2 |
| ResultTest | Result 响应封装 | common | 4 |
| **合计** | | | **54** |

### 11.5 测试模式

遵循 **Arrange-Act-Assert (AAA)** 模式：

```java
@Test
void register_validInput_returnsToken() {
    // Arrange - 准备测试数据
    RegisterDTO dto = new RegisterDTO();
    dto.setEmail(TEST_EMAIL);
    dto.setNickname("测试用户");

    when(userMapper.selectCount(any())).thenReturn(0L);
    when(userMapper.insert(any(User.class))).thenAnswer(inv -> {
        User user = inv.getArgument(0);
        user.setId(1L);
        return 1;
    });
    when(jwtUtil.generateToken(1L)).thenReturn(TEST_TOKEN);

    // Act - 执行被测方法
    String token = authService.register(dto);

    // Assert - 验证结果
    assertThat(token).isEqualTo(TEST_TOKEN);
}
```

### 11.6 测试组织

使用 `@Nested` 按业务场景分组：

```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Nested
    @DisplayName("发送验证码测试")
    class SendCodeTest { }

    @Nested
    @DisplayName("用户注册测试")
    class RegisterTest { }

    @Nested
    @DisplayName("用户登录测试")
    class LoginTest { }

    @Nested
    @DisplayName("获取当前用户测试")
    class GetCurrentUserTest { }
}
```

### 11.7 测试数据构建器

使用 `TestDataBuilder` 模式简化测试数据创建：

```java
User user = TestDataBuilder.user()
    .withId(1L)
    .withEmail("test@example.com")
    .build();

Post post = TestDataBuilder.post()
    .withContent("今天面试被拒了")
    .withUserId(1L)
    .build();
```

### 11.8 覆盖率要求

JaCoCo 配置最低覆盖率 **60%**（行覆盖率）：

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <configuration>
        <rules>
            <rule>
                <element>BUNDLE</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.60</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

### 11.9 集成测试配置

集成测试使用本地 MySQL + `test` Profile + `@Transactional` 自动回滚：

```yaml
# application-test.yml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/beijvzhihou_test?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root
  sql:
    init:
      mode: never
```

### 11.10 运行测试

```powershell
# 设置环境变量
$env:JAVA_HOME = "E:\ai\jdk-17.0.2"
$env:Path = "E:\ai\jdk-17.0.2\bin;E:\ai\apache-maven-3.9.12\bin;" + $env:Path

# 运行所有测试
mvn test

# 仅运行单元测试（快速反馈）
mvn test -Dtest="com.beijvzhihou.unit.**"

# 仅运行 Web 层测试
mvn test -Dtest="com.beijvzhihou.web.**"

# 仅运行集成测试
mvn test -Dtest="com.beijvzhihou.integration.**"

# 查看覆盖率报告
# 报告位置：backend/target/site/jacoco/index.html
```

### 11.11 测试结果

```
Tests run: 47, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

*文档版本：1.1 | 最后更新：2026-05-02*
