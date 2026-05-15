# 「被拒之后」浏览器测试实现计划

> **面向 AI 代理的工作者：** 使用 `agent-browser` 技能逐步执行此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。
>
> **目标：** 基于 `specs/2026-04-29-beijvzhihou-design.md` 第 6.1 节定义的 BR-1 ~ BR-13 浏览器交互规则，使用 `agent-browser` 编写并执行端到端浏览器测试，验证前端页面与后端 API 的交互行为符合设计规范。
>
> **测试工具：** `agent-browser`（Rust headless browser CLI）
>
> **前置条件：**
> - 后端服务已启动：`http://localhost:8080`
> - 前端服务已启动：`http://localhost:5173`（若端口被占用会自动切换到 5174）
> - `agent-browser` 已全局安装

---

## 测试结果总览

| 规则 | 描述 | 状态 | 备注 |
|------|------|------|------|
| BR-1 | 未登录用户只能浏览帖子 | ✅ 通过 | 匿名可见帖子列表，无发帖输入框 |
| BR-2 | 未登录用户可以点赞 | ✅ 通过 | 匿名可点赞，toggle 取消 |
| BR-3 | IP 哈希去重 | ✅ 通过 | 同一浏览器重复点赞只记录一次 |
| BR-4 | 登录后才能发帖和回复 | ✅ 通过 | 登录后发帖输入框可见 |
| BR-5 | 未登录点击发帖跳转登录页 | ✅ 通过 | 跳转 `/login?redirect=/post/new`，登录后回到发帖页 |
| BR-6 | 注册完毕后跳转到登录页 | ✅ 通过 | 跳转 `/login?registered=1`，显示"注册成功，请登录" |
| BR-7 | 首页默认展示最新帖子 | ✅ 通过 | 默认按最新排序，帖子列表非空 |
| BR-8 | 注册时邮箱格式校验 | ✅ 通过 | `123`、`123123@`、`1123123@qq.` 提示"邮箱格式不正确" |
| BR-9 | 注册时验证码错误提示 | ✅ 通过 | 错误验证码提示"验证码错误" |
| BR-10 | 注册时邮箱唯一性校验 | ✅ 通过 | 已注册邮箱再次注册提示"该邮箱已注册" |
| BR-11 | 注册时密码校验 | ✅ 通过 | <8位→"密码长度必须至少8位"；纯数字→"密码必须包含字母"；纯字母→"密码必须包含数字" |
| BR-12 | 登录使用邮箱+密码 | ✅ 通过 | 登录表单为邮箱+密码，无验证码 |
| BR-13 | 登录成功后默认跳转首页 | ✅ 通过 | 无 redirect 参数时跳转 `/`，导航栏显示用户昵称 |
| BR-14 | 发帖后自动跳转帖子详情页，AI 回复醒目展示 | ✅ 通过 | 发帖成功跳转 `/post/:id`；AI 回复区域渐变背景+脉冲图标；AI 未回复时显示加载动画"AI 正在赶来安慰你…" |

---

## 任务清单

### 任务 0：安装 agent-browser

- [x] 0.1 全局安装 agent-browser：`npm install -g agent-browser`
- [x] 0.2 安装浏览器引擎：`agent-browser install`
- [x] 0.3 验证安装：`agent-browser open https://example.com --headed && agent-browser snapshot -i && agent-browser close`

---

### 任务 1：BR-7 — 首页默认展示最新帖子

**规则：** 浏览器默认打开系统首页，首页默认展示最新的帖子内容

- [x] 1.1 打开首页：`agent-browser open http://localhost:5173`
- [x] 1.2 快照页面交互元素：`agent-browser snapshot -i`
- [x] 1.3 验证"最新"排序按钮处于激活状态
- [x] 1.4 验证帖子列表非空（至少存在一个帖子卡片元素）
- [x] 1.5 截图保存：`agent-browser screenshot screenshots/br7-home-default.png`

---

### 任务 2：BR-1 — 未登录用户只能浏览帖子

**规则：** 用户没有登录，只能浏览帖子，不能发帖子，不能回复帖子

- [x] 2.1 清除登录状态：`agent-browser eval "localStorage.removeItem('token')"`
- [x] 2.2 打开首页：`agent-browser open http://localhost:5173`
- [x] 2.3 快照交互元素：`agent-browser snapshot -i`
- [x] 2.4 验证发帖输入框（textarea）**不存在**（未登录时隐藏）
- [x] 2.5 验证"去登录"提示文字存在
- [x] 2.6 点击帖子进入详情页
- [x] 2.7 验证帖子内容可见
- [x] 2.8 截图保存：`agent-browser screenshot screenshots/br1-anonymous-browse.png`

---

### 任务 3：BR-2 + BR-3 — 未登录用户可以点赞 + IP 哈希去重

**规则：** 用户没有登录，可以点赞；用请求头作为唯一标识，一个浏览器点击多次，只记录一次

- [x] 3.1 清除登录状态
- [x] 3.2 打开帖子详情页
- [x] 3.3 记录当前点赞数
- [x] 3.4 点击点赞按钮
- [x] 3.5 验证点赞数 +1
- [x] 3.6 再次点击点赞按钮（toggle 取消）
- [x] 3.7 验证点赞数恢复原值
- [x] 3.8 截图保存：`agent-browser screenshot screenshots/br2-anonymous-like.png`

---

### 任务 4：BR-5 — 未登录点击发帖跳转登录页，登录后回到发帖页

**规则：** 用户发帖子，点击发帖子，系统发现没有登录直接跳转到登录页面。登录成功之后再回到发帖子页面

- [x] 4.1 清除登录状态
- [x] 4.2 打开首页
- [x] 4.3 点击 NavBar 中的"发帖"按钮
- [x] 4.4 验证跳转到 `/login?redirect=/post/new`
- [x] 4.5 执行登录流程（邮箱+密码）
- [x] 4.6 验证登录后跳转到 `/post/new`（发帖页）
- [x] 4.7 截图保存：`agent-browser screenshot screenshots/br5-redirect-to-login.png`

---

### 任务 5：BR-4 — 登录后才能发帖和回复

**规则：** 用户登录之后才能发帖子和回复别人的帖子

- [x] 5.1 登录用户
- [x] 5.2 打开首页，验证发帖输入框（textarea）**存在**
- [x] 5.3 填写帖子内容并发布
- [x] 5.4 验证帖子发布成功
- [x] 5.5 截图保存：`agent-browser screenshot screenshots/br4-logged-in-post.png`

---

### 任务 6：BR-6 — 注册完毕后跳转到登录页

**规则：** 用户注册完毕之后，直接跳转到登录页面

- [x] 6.1 清除登录状态
- [x] 6.2 打开注册页：`agent-browser open http://localhost:5173/register`
- [x] 6.3 填写邮箱、验证码、密码
- [x] 6.4 点击注册按钮
- [x] 6.5 验证跳转到 `/login?registered=1`
- [x] 6.6 验证"注册成功，请登录"提示文字可见
- [x] 6.7 截图保存：`agent-browser screenshot screenshots/br6-register-redirect.png`

---

### 任务 8：BR-8 — 注册时邮箱格式校验

**规则：** 注册页输入不合法邮箱时提示"邮箱格式不正确"

- [x] 8.1 打开注册页
- [x] 8.2 输入不合法邮箱 `123` → 触发 blur → 验证提示"邮箱格式不正确"
- [x] 8.3 输入 `123123@` → 触发 blur → 验证提示"邮箱格式不正确"
- [x] 8.4 输入 `1123123@qq.` → 触发 blur → 验证提示"邮箱格式不正确"
- [x] 8.5 输入合法邮箱 `test@example.com` → 触发 blur → 验证无错误提示

---

### 任务 9：BR-9 — 注册时验证码错误提示

**规则：** 输入错误验证码时提示"验证码错误"

- [x] 9.1 打开注册页，填写合法邮箱和密码
- [x] 9.2 发送验证码
- [x] 9.3 输入错误验证码 `123` → 点击注册 → 验证提示"验证码错误"
- [x] 9.4 输入错误验证码 `1234` → 点击注册 → 验证提示"验证码错误"
- [x] 9.5 输入错误验证码 `123456` → 点击注册 → 验证提示"验证码错误"
- [x] 9.6 截图保存：`agent-browser screenshot screenshots/br9-code-error.png`

---

### 任务 10：BR-10 — 注册时邮箱唯一性校验

**规则：** 已注册邮箱再次注册时提示"该邮箱已注册"

- [x] 10.1 通过 API 或数据库创建已注册用户
- [x] 10.2 打开注册页，填写已注册邮箱
- [x] 10.3 发送验证码并输入正确验证码
- [x] 10.4 点击注册 → 验证提示"该邮箱已注册"
- [x] 10.5 截图保存：`agent-browser screenshot screenshots/br10-email-duplicate.png`

---

### 任务 11：BR-11 — 注册时密码校验

**规则：** 密码长度必须 ≥ 8 位，必须包含数字和字母；不符合时提示具体原因

- [x] 11.1 打开注册页，输入密码 `Abc123`（<8位）→ blur → 验证提示"密码长度必须至少8位"
- [x] 11.2 输入密码 `12345678`（纯数字）→ blur → 验证提示"密码必须包含字母"
- [x] 11.3 输入密码 `abcdefgh`（纯字母）→ blur → 验证提示"密码必须包含数字"
- [x] 11.4 截图保存：`agent-browser screenshot screenshots/br11-password-no-digit.png`

---

### 任务 12：BR-12 + BR-13 — 登录使用邮箱+密码 + 默认跳转首页

**规则：** 登录表单为邮箱+密码；无 redirect 参数时登录成功跳转 `/`（首页）

- [x] 12.1 打开登录页：`agent-browser open http://localhost:5174/login`
- [x] 12.2 验证登录表单只有邮箱+密码（无验证码输入框）
- [x] 12.3 输入邮箱和密码
- [x] 12.4 点击登录按钮
- [x] 12.5 验证跳转到首页 `/`
- [x] 12.6 验证导航栏显示用户昵称和退出按钮
- [x] 12.7 验证 localStorage 中保存了 token
- [x] 12.8 截图保存：`agent-browser screenshot screenshots/br12-13-login-success-homepage.png`

---

## 测试中发现并修复的 Bug

### 1. 路由路径不匹配

- **问题：** PostCard.vue 中链接为 `/post/:id`，但路由定义是 `/posts/:id`
- **修复：** 将路由定义改为 `/post/:id` 以匹配组件链接

### 2. post_like 表缺少 ip_hash 列

- **问题：** 匿名点赞需要 IP 哈希去重，但数据库表缺少 `ip_hash` 列
- **修复：** `ALTER TABLE post_like ADD COLUMN ip_hash varchar(64) NULL AFTER user_id;`

### 3. 认证拦截器未正确处理 GET 请求

- **问题：** 拦截器配置未包含所有需要认证的路径
- **修复：** 更新 WebMvcConfig 中的拦截路径配置

### 4. 前端未正确处理业务错误码

- **问题：** 后端返回 HTTP 200 但 code 非 200 时，前端未正确显示错误信息
- **修复：** 更新 axios 响应拦截器，对 `data.code !== 200` 的情况执行 `Promise.reject(data)`

### 5. LoginView token 提取错误

- **问题：** `res.token` 应为 `res.data.token`（响应拦截器返回的是 `{ code, message, data }` 结构）
- **修复：** 将 `userStore.setToken(res.token)` 改为 `userStore.setToken(res.data.token)`

---

## agent-browser 使用案例总结

### 常用命令速查

```powershell
# 打开页面
agent-browser open http://localhost:5173

# 获取页面交互元素快照（含 ref 编号）
agent-browser snapshot -i

# 输入文本到指定元素
agent-browser type e6 "hello@example.com"

# 点击指定元素
agent-browser click e8

# 执行 JavaScript 表达式
agent-browser eval "document.querySelector('button.btn-primary').click()"
agent-browser eval "window.location.href"
agent-browser eval "localStorage.getItem('token')"

# 截图保存
agent-browser screenshot screenshots/test-case.png

# 清除 localStorage
agent-browser eval "localStorage.removeItem('token')"
```

### 关键经验与踩坑记录

#### 1. `agent-browser click` 不触发 Vue 组件事件

**问题：** `agent-browser click e12` 命令点击按钮时，DOM 元素获得了焦点但未触发 Vue 的 `@click` 事件处理器。

**原因：** `agent-browser click` 使用的是 Playwright 底层的 DOM 点击，可能不会正确触发 Vue 的事件代理机制（特别是当按钮设置了 `:disabled` 动态绑定或使用了事件修饰符时）。

**解决方案：** 使用 `agent-browser eval` 直接调用 JavaScript 的 `click()` 方法：

```powershell
# ❌ 不可靠 — 可能不触发 Vue 事件
agent-browser click e12

# ✅ 可靠 — 直接触发 DOM click 事件
agent-browser eval "document.querySelector('button.btn-primary').click()"
```

#### 2. Vue 表单 blur 事件需要手动触发

**问题：** 使用 `agent-browser type` 输入文本后，Vue 的 `@blur` 验证不会自动触发（因为元素没有真正失去焦点）。

**解决方案：** 输入完成后手动调用 `blur()`：

```powershell
agent-browser type e9 "12345678"
agent-browser eval "document.querySelectorAll('input[type=password]')[0].blur()"
```

#### 3. 验证码获取方案

**问题：** 验证码通过 `log.info` 输出到后端控制台，agent-browser 无法直接读取。

**解决方案（按推荐顺序）：**

1. **临时调试端点**：在后端添加 `/api/auth/debug/code?email=xxx` 端点，返回内存中的验证码（测试完成后删除）
2. **直接调用后端 API**：用 `Invoke-RestMethod` 发送验证码，再通过调试端点获取
3. **数据库插入**：直接用 SQL 插入测试用户，绕过注册流程

```powershell
# 方案 1：发送验证码 → 调试端点获取
$body = '{"email":"test@example.com","type":"register"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/send-code" -Method Post -ContentType "application/json" -Body $body
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/debug/code?email=test@example.com" -Method Get

# 方案 3：直接 SQL 插入测试用户
mysql -u root -proot beijvzhihou -e "INSERT INTO user (email, email_masked, password_hash, nickname, avatar_url, status) VALUES ('test@example.com', 't***@example.com', '$2a$10$...', '测试', 'https://picsum.photos/seed/1', 1);"
```

#### 4. HMR 热更新可能不更新已挂载组件

**问题：** 修改 Vue 组件代码后，Vite HMR 更新了模块，但浏览器中已挂载的组件实例未重新渲染。

**解决方案：** 强制刷新页面：

```powershell
agent-browser eval "window.location.reload()"
# 或重新打开页面
agent-browser open http://localhost:5174/login
```

#### 5. Vite 端口可能自动切换

**问题：** 当 5173 端口被占用时，Vite 会自动使用 5174 等端口。

**解决方案：** 启动前端后检查实际端口，后续测试使用正确的端口号。

#### 6. 后端 API 直接测试

**问题：** 有时需要验证后端逻辑是否正确，而不依赖前端。

**解决方案：** 使用 PowerShell 的 `Invoke-RestMethod` 直接调用 API：

```powershell
# 登录 API 测试
$body = '{"email":"test@example.com","password":"Test1234"}'
try {
    $r = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body $body
    $r | ConvertTo-Json
} catch {
    $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
    $reader.ReadToEnd()
}

# 注册 API 测试（含错误响应）
$body = '{"email":"dup@example.com","code":"518040","password":"Test1234"}'
try {
    $r = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body $body
    $r | ConvertTo-Json
} catch {
    "Status: " + $_.Exception.Response.StatusCode.value__
    $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
    $reader.ReadToEnd()
}
```

#### 7. JDK 版本问题

**问题：** Spring Boot 3.x 需要 JDK 17+，但系统默认可能是 JDK 8。

**解决方案：** 启动后端时显式设置 JAVA_HOME：

```powershell
$env:JAVA_HOME = "E:\ai\jdk-17.0.2"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
mvn spring-boot:run
```

---

## 测试环境准备

### 启动后端

```powershell
cd c:\Users\conca\.qclaw\workspace\.worktrees\beijvzhihou-backend\backend
$env:JAVA_HOME = "E:\ai\jdk-17.0.2"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
mvn spring-boot:run
```

### 启动前端

```powershell
cd c:\Users\conca\.qclaw\workspace\.worktrees\beijvzhihou-backend\frontend
npm run dev
```

### 测试数据

确保数据库中至少存在：
- 1 条已发布的帖子（status=1）
- 1 个已注册用户（用于登录测试）

---

## 截图目录

```
screenshots/
├── br7-home-default.png                # BR-7 首页默认展示
├── br1-anonymous-browse.png            # BR-1 匿名浏览
├── br2-anonymous-like.png              # BR-2/3 匿名点赞
├── br5-redirect-to-login.png           # BR-5 跳转登录页
├── br5-back-to-post-new.png            # BR-5 登录后回到发帖页
├── br4-logged-in-post.png              # BR-4 登录发帖
├── br6-register-redirect.png           # BR-6 注册跳转
├── br9-code-error.png                  # BR-9 验证码错误提示
├── br9-debug.png                       # BR-9 调试截图
├── br10-email-duplicate.png            # BR-10 邮箱重复注册
├── br11-password-no-digit.png          # BR-11 密码校验（纯字母）
└── br12-13-login-success-homepage.png  # BR-12/13 登录成功跳转首页
```

---

## 规则与设计文档对照

| 规则编号 | 设计文档位置 | 测试任务 | 状态 |
|---------|------------|---------|------|
| BR-1 | specs/2026-04-29-beijvzhihou-design.md §6.1 | 任务 2 | ✅ |
| BR-2 | specs/2026-04-29-beijvzhihou-design.md §6.1 | 任务 3 | ✅ |
| BR-3 | specs/2026-04-29-beijvzhihou-design.md §6.1 | 任务 3 | ✅ |
| BR-4 | specs/2026-04-29-beijvzhihou-design.md §6.1 | 任务 5 | ✅ |
| BR-5 | specs/2026-04-29-beijvzhihou-design.md §6.1 | 任务 4 | ✅ |
| BR-6 | specs/2026-04-29-beijvzhihou-design.md §6.1 | 任务 6 | ✅ |
| BR-7 | specs/2026-04-29-beijvzhihou-design.md §6.1 | 任务 1 | ✅ |
| BR-8 | specs/2026-04-29-beijvzhihou-design.md §6.1 | 任务 8 | ✅ |
| BR-9 | specs/2026-04-29-beijvzhihou-design.md §6.1 | 任务 9 | ✅ |
| BR-10 | specs/2026-04-29-beijvzhihou-design.md §6.1 | 任务 10 | ✅ |
| BR-11 | specs/2026-04-29-beijvzhihou-design.md §6.1 | 任务 11 | ✅ |
| BR-12 | specs/2026-04-29-beijvzhihou-design.md §6.1 | 任务 12 | ✅ |
| BR-13 | specs/2026-04-29-beijvzhihou-design.md §6.1 | 任务 12 | ✅ |
| BR-14 | specs/2026-04-29-beijvzhihou-design.md §6.1 | 任务 17 | ✅ |

---

## 补充测试：关键页面流程测试

> 2026-05-02 补充：对登录后个人信息、发帖、帖子详情、退出登录、管理后台等关键页面进行完整流程测试。

### 任务 13：个人信息页面测试

- [x] 13.1 登录后点击"个人中心"导航链接
- [x] 13.2 验证个人信息页显示：昵称、脱敏邮箱、注册日期
- [x] 13.3 验证统计卡片：发布帖子数、获得点赞数、AI 安慰数
- [x] 13.4 验证"我的帖子"列表显示用户发布的帖子
- [x] 13.5 点击"编辑资料"按钮，修改昵称并保存
- [x] 13.6 验证昵称更新成功
- [x] 13.7 截图保存：`screenshots/profile-page-fixed.png`、`screenshots/profile-edit-success.png`

**修复的 Bug：**
- UserVO 缺少 `createdAt` 字段 → 添加字段并更新构造函数
- ProfileView 模板中 `email` 应为 `emailMasked` → 修正字段名
- App.vue 未在页面加载时初始化用户状态 → 添加 `fetchUser()` 调用
- ProfileView 退出登录后未自动跳转 → 添加 `watch(userStore.isLoggedIn)` 监听

### 任务 14：帖子详情页测试

- [x] 14.1 从首页点击帖子卡片进入详情页
- [x] 14.2 验证帖子内容、作者昵称、发布时间正确显示
- [x] 14.3 验证 AI 回复内容显示
- [x] 14.4 点击点赞按钮，验证点赞数更新
- [x] 14.5 截图保存：`screenshots/post-detail-page.png`、`screenshots/post-detail-like.png`

### 任务 15：退出登录测试

- [x] 15.1 点击导航栏"退出"按钮
- [x] 15.2 验证 localStorage token 已清除
- [x] 15.3 验证导航栏切换为"登录"/"注册"链接
- [x] 15.4 刷新页面后访问 /profile 自动跳转到 `/login?redirect=/profile`
- [x] 15.5 截图保存

### 任务 16：管理后台页面测试

- [x] 16.1 为 User 实体添加 `role` 字段（数据库 + Java 实体 + UserVO）
- [x] 16.2 设置测试用户为 ADMIN 角色
- [x] 16.3 重新登录，验证导航栏显示"⚙️ 管理后台"链接
- [x] 16.4 点击进入管理后台页面
- [x] 16.5 验证统计卡片：总帖子数、总用户数、今日新增、待审核
- [x] 16.6 验证标签页切换：待审核、已通过、用户管理
- [x] 16.7 验证用户管理标签页显示用户角色（管理员/普通用户）
- [x] 16.8 截图保存：`screenshots/admin-page.png`、`screenshots/admin-users-tab.png`

**修复的 Bug：**
- User 实体缺少 `role` 字段 → 数据库添加 `role` 列 + 实体添加字段
- UserVO 缺少 `role` 字段 → 添加字段并更新所有构造函数调用

### 任务 17：BR-14 — 发帖后自动跳转帖子详情页，AI 回复醒目展示

**规则：** 发帖成功后自动跳转到帖子详情页；AI 回复区域需醒目设计（渐变背景/图标/动画）；AI 未回复时显示加载动画

- [x] 17.1 登录用户在首页发帖，输入内容并点击发布
- [x] 17.2 验证发帖成功后自动跳转到 `/post/:id`（帖子详情页）
- [x] 17.3 验证帖子详情页正确显示帖子内容
- [x] 17.4 验证 AI 回复区域使用渐变背景、脉冲图标等醒目设计
- [x] 17.5 验证 AI 已回复时，回复内容正常显示
- [x] 17.6 验证 AI 未回复时，显示加载动画"AI 正在赶来安慰你…"
- [x] 17.7 截图保存：`screenshots/br14-post-redirect-detail.png`、`screenshots/br14-ai-loading-animation.png`、`screenshots/br14-ai-reply-highlight.png`

**修改的文件：**
- `HomeView.vue` — 发帖成功后从刷新首页改为 `router.push(/post/${postId})` 跳转到详情页
- `PostDetailView.vue` — AI 回复区域重新设计：
  - 渐变背景容器（indigo → blue 渐变）
  - 🤖 图标脉冲动画
  - 标题使用渐变文字效果
  - AI 回复卡片滑入动画
  - AI 未回复时显示跳动圆点 + "AI 正在赶来安慰你…" 加载提示
  - 轮询机制：每 3 秒检查 AI 回复状态，最多 60 秒

### 补充测试截图

```
screenshots/
├── profile-page-fixed.png          # 个人信息页（修复后）
├── profile-edit-success.png        # 编辑资料成功
├── post-create-success.png         # 发帖成功
├── post-detail-page.png            # 帖子详情页
├── post-detail-like.png            # 帖子点赞
├── profile-with-posts.png          # 个人信息页含帖子
├── admin-page.png                  # 管理后台首页
├── admin-users-tab.png             # 管理后台-用户管理
├── br14-post-redirect-detail.png   # BR-14 发帖后跳转详情页
├── br14-ai-loading-animation.png   # BR-14 AI 加载动画
└── br14-ai-reply-highlight.png     # BR-14 AI 回复醒目展示
```
