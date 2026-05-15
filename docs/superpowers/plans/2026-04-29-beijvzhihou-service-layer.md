# 「被拒之后」后端 Service 层实现计划

> 版本：1.0  
> 日期：2026-04-29  
> 分支：`feature/beijvzhihou-backend`  
> 工作树：`C:\Users\conca\.qclaw\workspace\.worktrees\beijvzhihou-backend`

---

## 概述

本计划实现后端 Service 层，包括：认证（邮箱注册/登录/JWT）、帖子（发帖/Feed/点赞）、内容审核（关键词+AI异步）、管理后台。

**前置依赖：** 任务 1（Maven初始化）和 任务 3（统一响应/异常）已完成，所有实体类和 Mapper 已就绪。

---

## 任务清单

### 任务 1：DTO 层 — 请求/响应对象
**并行度：** 可与任务 2 并行  
**文件：**
- `backend/src/main/java/com/beijvzhihou/dto/AuthDTO.java` — 登录请求
- `backend/src/main/java/com/beijvzhihou/dto/RegisterDTO.java` — 注册请求
- `backend/src/main/java/com/beijvzhihou/dto/PostCreateDTO.java` — 发帖请求
- `backend/src/main/java/com/beijvzhihou/dto/UserVO.java` — 用户信息（脱敏）
- `backend/src/main/java/com/beijvzhihou/dto/PostVO.java` — 帖子详情（包含 AI 回复列表）
- `backend/src/main/java/com/beijvzhihou/dto/PageResult.java` — 分页结果封装

**实现要点：**
- `AuthDTO`：email, code
- `RegisterDTO`：email, code, nickname（可选，不传则随机生成）
- `PostCreateDTO`：content（最大500字）
- `UserVO`：id, emailMasked, nickname, avatarUrl（不含 passwordHash/id 等敏感字段）
- `PostVO`：id, content, nickname（发帖人匿名昵称）, avatarUrl, emotionScoreAvg, likeCount, viewCount, createdAt, aiReplies（List\<AiReplyVO\>）
- `PageResult`：\<T\> list, long total, int page, int size

### 任务 2：JWT 工具 + 安全过滤器
**并行度：** 可与任务 1 并行  
**文件：**
- `backend/src/main/java/com/beijvzhihou/util/JwtUtil.java` — JWT 工具类
- `backend/src/main/java/com/beijvzhihou/config/SecurityConfig.java` — 安全配置
- `backend/src/main/java/com/beijvzhihou/filter/JwtAuthenticationFilter.java` — JWT 过滤器
- `backend/src/main/java/com/beijvzhihou/interceptor/AuthInterceptor.java` — 登录拦截器

**实现要点：**
- `JwtUtil`：使用 JJWT 库（0.12.x），签发/解析 token，token 包含 userId，7天过期
- `SecurityConfig`：公开 `/api/auth/**` `/api/posts`（GET） `/api/posts/:id`（GET），拦截其他所有
- `JwtAuthenticationFilter`：从 Authorization: Bearer xxx 提取 token，解析出 userId 存入 SecurityContext
- `AuthInterceptor`：检查用户是否已登录，未登录返回 401
- **pom.xml 依赖**：添加 `jjwt-api`, `jjwt-impl`, `jjwt-jackson` 0.12.x

### 任务 3：认证模块 — AuthService + AuthController
**依赖：** 任务 1、任务 2  
**文件：**
- `backend/src/main/java/com/beijvzhihou/service/impl/AuthServiceImpl.java`
- `backend/src/main/java/com/beijvzhihou/service/AuthService.java`
- `backend/src/main/java/com/beijvzhihou/controller/AuthController.java`

**AuthService 接口：**
```java
public interface AuthService {
    // 发送验证码（注册/登录共用，type 区分）
    void sendCode(String email, String type);
    // 注册
    String register(RegisterDTO dto);
    // 登录
    String login(AuthDTO dto);
    // 获取当前用户
    User getCurrentUser(Long userId);
}
```

**AuthController 接口：**
- `POST /api/auth/send-code` — body: `{email, type}`，60秒内同一IP限一次
- `POST /api/auth/register` — body: `{email, code, nickname?}`
- `POST /api/auth/login` — body: `{email, code}`
- `POST /api/auth/logout` — 清除 Redis token（可选）

**实现要点：**
- 验证码：6位随机数，存入 Redis，key = `email:code:{email}`，5分钟过期
- 注册：验证邮箱+验证码+类型，密码随机生成（用户无需设密码，验证码即密码），注册后返回 JWT
- 登录：验证邮箱+验证码+类型，登录成功返回 JWT
- 昵称随机生成：从预设词库（伤心的向日葵/不服输的猫/追光的蜗牛/倔强的小草/不服命的星星/……）随机选取
- 头像：从 picsum.photos 随机图片 URL
- 注册防刷：Redis 计数，key = `ip:send:{ip}`，60秒内只能发一次

### 任务 4：帖子模块 — PostService + PostController
**依赖：** 任务 1  
**文件：**
- `backend/src/main/java/com/beijvzhihou/service/impl/PostServiceImpl.java`
- `backend/src/main/java/com/beijvzhihou/service/PostService.java`
- `backend/src/main/java/com/beijvzhihou/controller/PostController.java`

**PostService 接口：**
```java
public interface PostService {
    // 发帖（触发 AI 回复）
    Post createPost(Long userId, PostCreateDTO dto);
    // 帖子列表（分页+排序）
    PageResult<PostVO> listPosts(int page, int size, String sort);
    // 帖子详情
    PostVO getPost(Long postId);
    // 点赞/取消点赞
    void toggleLike(Long postId, Long userId, String ipHash);
}
```

**PostController 接口：**
- `GET /api/posts` — page, size, sort（time/hot）
- `GET /api/posts/:id` — 帖子详情
- `POST /api/posts` — body: `{content}`，需登录
- `POST /api/posts/:id/like` — body: `{userId?}`, ipHash 用于匿名点赞

**实现要点：**
- 发帖：内容最大500字，关键词过滤，存入 DB（status=1），异步触发 AI
- AI 调度：发帖后立即调用 AI（同步），DeepSeek 必回，Kimi2/Qwen 按情绪阈值判断
- AI 调用顺序：DeepSeek → Kimi2 → Qwen（逐个等待）
- 帖子详情：浏览数+1（updateLikeCount），返回 PostVO + aiReplies
- 点赞：已赞则取消，未赞则新增；更新 post.like_count
- 排序：time=按 createdAt DESC，hot=按 likeCount DESC

### 任务 5：内容审核 — ReviewService + AdminController
**依赖：** 任务 1  
**文件：**
- `backend/src/main/java/com/beijvzhihou/service/impl/ReviewServiceImpl.java`
- `backend/src/main/java/com/beijvzhihou/service/ReviewService.java`
- `backend/src/main/java/com/beijvzhihou/controller/AdminController.java`
- `backend/src/main/java/com/beijvzhihou/util/KeywordFilter.java` — 关键词过滤工具

**ReviewService 接口：**
```java
public interface ReviewService {
    // 关键词过滤
    boolean keywordFilter(String content);
    // AI 异步审核
    void asyncReview(Long postId);
    // 管理员：获取待审核列表
    List<Post> getPendingPosts();
    // 管理员：审核帖子
    void reviewPost(Long postId, String result);
}
```

**AdminController 接口：**
- `GET /api/admin/review/pending` — 待审核帖子列表
- `POST /api/admin/review/:id` — body: `{result: "pass"|"reject"}`
- `GET /api/admin/stats` — 数据统计

**实现要点：**
- 关键词过滤：正则匹配，命中则直接拒绝发帖（返回 422）
- 关键词列表：存放在 `resources/keywords.txt`（每行一个），加载到内存
- AI 审核：发帖时同步调用（设计文档说"异步"，MVP 先做同步，后续可改）
- 管理员：手动审核列表，审核后更新 post.status
- 统计数据：总帖子数、总用户数、今日发帖数

---

## 验收标准

- 所有接口返回 `Result<T>` 统一格式
- JWT token 正确签发和验证
- 验证码 Redis 存储，5分钟有效
- 关键词过滤命中直接拒绝
- 发帖触发 AI 回复流程
- 点赞去重（同一用户/同IP）
- 编译 0 错误

---

## 不在本计划范围

- AI 集成（DeepSeek/Kimi2/Qwen 的 Spring AI 配置）— 后续计划
- 前端页面 — 后续计划
- 邮件发送服务（验证码邮件发送）— MVP 阶段可先用日志输出验证码
