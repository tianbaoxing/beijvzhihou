# 「被拒之后」后端初始化 + 基础结构 实现计划

> **面向 AI 代理的工作者：** 必需子技能：`superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans` 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。
>
> **目标：** 初始化 Spring Boot 后端项目，搭建基础架构（分层结构、统一响应、异常处理、数据库配置）。
>
> **架构：** Spring Boot 3.4 + Maven，标准三层架构（Controller → Service → Mapper），RESTful API 风格。
>
> **技术栈：** Spring Boot 3.4 / Spring Web / MyBatis-Plus / MySQL / Redis / Lombok / Hutool
>
> **前置条件：** JDK 17 已在终端 PATH 中：`$env:Path = "E:\ai\jdk-17.0.2\bin;E:\ai\node-v20.19.4-win-x64;" + $env:Path`

---

## 文件结构

```
backend/
├── pom.xml                                    # Maven 依赖配置
├── src/main/java/com/beijvzhihou/
│   ├── BeijvzhihouApplication.java            # 启动类
│   ├── config/
│   │   ├── WebConfig.java                    # 跨域 + Web 配置
│   │   ├── MybatisPlusConfig.java            # MyBatis-Plus 配置
│   │   └── RedisConfig.java                  # Redis 配置
│   ├── common/
│   │   ├── Result.java                       # 统一响应封装
│   │   ├── ResultCode.java                   # 响应码枚举
│   │   └── GlobalExceptionHandler.java        # 全局异常处理
│   ├── entity/
│   │   ├── User.java                         # 用户实体
│   │   ├── Post.java                         # 帖子实体
│   │   ├── AiReply.java                      # AI 回复实体
│   │   ├── PostLike.java                     # 点赞实体
│   │   ├── EmailCode.java                    # 邮箱验证码实体
│   │   └── ContentReview.java                # 审核日志实体
│   ├── mapper/
│   │   ├── UserMapper.java
│   │   ├── PostMapper.java
│   │   ├── AiReplyMapper.java
│   │   ├── PostLikeMapper.java
│   │   ├── EmailCodeMapper.java
│   │   └── ContentReviewMapper.java
│   └── controller/
│       └── TestController.java                # 测试 Controller
├── src/main/resources/
│   ├── application.yml                       # 主配置文件
│   └── application-dev.yml                   # 开发环境配置
└── src/test/java/com/beijvzhihou/
    └── common/
        └── ResultTest.java                   # 统一响应测试
```

---

## 数据库 SQL

执行以下 SQL 创建数据库和表：

```sql
CREATE DATABASE IF NOT EXISTS beijvzhihou DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE beijvzhihou;

CREATE TABLE user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    email           VARCHAR(255) NOT NULL UNIQUE COMMENT '邮箱（AES加密存储）',
    email_masked    VARCHAR(255) NOT NULL COMMENT '脱敏展示：c***a@qq.com',
    nickname        VARCHAR(50) NOT NULL COMMENT '随机匿名昵称',
    avatar_url      VARCHAR(500) COMMENT '随机头像 URL',
    password_hash   VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密密码',
    status          TINYINT DEFAULT 1 COMMENT '1=正常 0=禁用',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE post (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id             BIGINT NOT NULL,
    content             TEXT NOT NULL COMMENT '帖子内容（500字以内）',
    emotion_score_avg   DECIMAL(3,1) DEFAULT 0 COMMENT 'AI 平均情绪分',
    ai_response_count   INT DEFAULT 0 COMMENT 'AI 回复数',
    like_count          INT DEFAULT 0 COMMENT '点赞数缓存',
    view_count          INT DEFAULT 0 COMMENT '浏览数',
    status              TINYINT DEFAULT 1 COMMENT '1=正常 2=审核中 0=已删除',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at),
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ai_reply (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id         BIGINT NOT NULL,
    ai_provider     VARCHAR(50) NOT NULL COMMENT 'DeepSeek / Kimi2 / Qwen',
    perspective     VARCHAR(50) NOT NULL COMMENT '心情 / 历史 / 技术',
    emotion_score   DECIMAL(3,1) COMMENT '该AI评估的情绪分',
    content         TEXT NOT NULL COMMENT 'AI 回复内容',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post (post_id),
    FOREIGN KEY (post_id) REFERENCES post(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE post_like (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id     BIGINT NOT NULL,
    user_id     BIGINT COMMENT 'NULL=匿名点赞',
    ip_hash     VARCHAR(64) COMMENT '浏览器指纹（fingerprint），用于未登录点赞去重',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_ip (post_id, ip_hash),
    UNIQUE KEY uk_post_user (post_id, user_id),
    FOREIGN KEY (post_id) REFERENCES post(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE email_code (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    email       VARCHAR(255) NOT NULL,
    code        VARCHAR(10) NOT NULL,
    type        VARCHAR(20) COMMENT 'register / login / reset',
    expires_at  DATETIME NOT NULL,
    used        TINYINT DEFAULT 0,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE content_review (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id       BIGINT,
    review_type   VARCHAR(20) COMMENT 'keyword / ai / manual',
    result        VARCHAR(20) COMMENT 'pass / reject',
    reason        VARCHAR(500),
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 任务清单

---

### 任务 1：创建 Maven 项目结构 + pom.xml

**文件：**
- 创建：`backend/pom.xml`

- [ ] **步骤 1：创建 backend 目录**

PowerShell：
```powershell
cd C:\Users\conca\.qclaw\workspace
mkdir backend\src\main\java\com\beijvzhihou\config
mkdir backend\src\main\java\com\beijvzhihou\common
mkdir backend\src\main\java\com\beijvzhihou\entity
mkdir backend\src\main\java\com\beijvzhihou\mapper
mkdir backend\src\main\java\com\beijvzhihou\controller
mkdir backend\src\main\java\com\beijvzhihou\service
mkdir backend\src\main\resources
mkdir backend\src\test\java\com\beijvzhihou\common
```

- [ ] **步骤 2：编写 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.0</version>
        <relativePath/>
    </parent>

    <groupId>com.beijvzhihou</groupId>
    <artifactId>beijvzhihou-backend</artifactId>
    <version>1.0.0</version>
    <name>beijvzhihou-backend</name>
    <description>被拒之后 - 后端服务</description>

    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.9</mybatis-plus.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MySQL 驱动 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Hutool 工具库 -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.8.29</version>
        </dependency>

        <!-- JJWT (JWT 认证) -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.6</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Mail (邮件发送) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>

        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **步骤 3：验证 pom.xml 语法**

```powershell
cd C:\Users\conca\.qclaw\workspace\backend
$env:Path = "E:\ai\jdk-17.0.2\bin;" + $env:Path
mvn validate
```

预期：BUILD SUCCESS

- [ ] **步骤 4：Commit**

```bash
git add backend/pom.xml
git commit -m "feat: init backend project structure and pom.xml"
```

---

### 任务 2：创建 application.yml 配置文件

**文件：**
- 创建：`backend/src/main/resources/application.yml`
- 创建：`backend/src/main/resources/application-dev.yml`

- [ ] **步骤 1：编写 application.yml（主配置）**

```yaml
spring:
  profiles:
    active: dev
  application:
    name: beijvzhihou

  # 文件上传大小限制
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB

# 统一日志配置
logging:
  level:
    com.beijvzhihou: DEBUG
    com.baomidou.mybatisplus: DEBUG
```

- [ ] **步骤 2：编写 application-dev.yml（开发环境）**

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/beijvzhihou?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: ${DB_PASSWORD:}

  # Redis 配置（本地无Redis可先注掉）
  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD:}
      database: 0

  # 邮件配置（后续填入）
  mail:
    host: smtp.qq.com
    port: 587
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

# MyBatis-Plus 配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.beijvzhihou.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# 服务器配置
server:
  port: 8080

# 自定义配置
app:
  jwt:
    secret: ${JWT_SECRET:beijvzhihou-secret-key-change-in-production}
    expire-days: 7
  email:
    code-expire-minutes: 5
```

- [ ] **步骤 3：Commit**

```bash
git add backend/src/main/resources/
git commit -m "feat: add application.yml and application-dev.yml"
```

---

### 任务 3：启动类 + 全局统一响应

**文件：**
- 创建：`backend/src/main/java/com/beijvzhihou/BeijvzhihouApplication.java`
- 创建：`backend/src/main/java/com/beijvzhihou/common/Result.java`
- 创建：`backend/src/main/java/com/beijvzhihou/common/ResultCode.java`
- 创建：`backend/src/main/java/com/beijvzhihou/common/GlobalExceptionHandler.java`

- [ ] **步骤 1：编写 BeijvzhihouApplication.java**

```java
package com.beijvzhihou;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.beijvzhihou.mapper")
@EnableAsync
public class BeijvzhihouApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeijvzhihouApplication.class, args);
    }
}
```

- [ ] **步骤 2：编写 Result.java（统一响应封装）**

```java
package com.beijvzhihou.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(ResultCode.FAIL.getCode(), message, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
}
```

- [ ] **步骤 3：编写 ResultCode.java（响应码枚举）**

```java
package com.beijvzhihou.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    
    // 参数错误 400
    PARAM_ERROR(400, "参数错误"),
    
    // 认证错误 401
    UNAUTHORIZED(401, "未登录或登录已过期"),
    CODE_EXPIRED(401, "验证码已过期"),
    CODE_ERROR(401, "验证码错误"),
    
    // 权限错误 403
    FORBIDDEN(403, "无权限访问"),
    
    // 资源不存在 404
    NOT_FOUND(404, "资源不存在"),
    
    // 业务错误 422
    EMAIL_ALREADY_EXISTS(422, "该邮箱已注册"),
    EMAIL_NOT_FOUND(422, "该邮箱未注册"),
    POST_NOT_FOUND(422, "帖子不存在"),
    
    // 频率限制 429
    RATE_LIMITED(429, "操作太频繁，请稍后再试"),
    ;

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

- [ ] **步骤 4：编写 GlobalExceptionHandler.java**

```java
package com.beijvzhihou.common;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        log.warn("参数校验异常: {}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        String message = e.getFieldError() != null
                ? e.getFieldError().getDefaultMessage()
                : "参数绑定失败";
        log.warn("参数绑定异常: {}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.FAIL.getCode(), "系统繁忙，请稍后再试");
    }
}
```

- [ ] **步骤 5：编写 BusinessException.java（自定义业务异常）**

```java
package com.beijvzhihou.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
}
```

- [ ] **步骤 6：验证编译**

```powershell
cd C:\Users\conca\.qclaw\workspace\backend
$env:Path = "E:\ai\jdk-17.0.2\bin;" + $env:Path
mvn compile -q
```

预期：BUILD SUCCESS（无输出）

- [ ] **步骤 7：Commit**

```bash
git add backend/src/main/java/com/beijvzhihou/BeijvzhihouApplication.java
git add backend/src/main/java/com/beijvzhihou/common/
git commit -m "feat: add application class, Result, ResultCode, GlobalExceptionHandler"
```

---

### 任务 4：Config 配置类（跨域 + MyBatis-Plus + Redis）

**文件：**
- 创建：`backend/src/main/java/com/beijvzhihou/config/WebConfig.java`
- 创建：`backend/src/main/java/com/beijvzhihou/config/MybatisPlusConfig.java`
- 创建：`backend/src/main/java/com/beijvzhihou/config/RedisConfig.java`

- [ ] **步骤 1：编写 WebConfig.java（跨域配置）**

```java
package com.beijvzhihou.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // 前端开发地址，允许所有开发端口
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

- [ ] **步骤 2：编写 MybatisPlusConfig.java**

```java
package com.beijvzhihou.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

- [ ] **步骤 3：编写 RedisConfig.java（可先注掉 Redis 部分，后面 AI 限流时再启用）**

```java
package com.beijvzhihou.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
```

- [ ] **步骤 4：Commit**

```bash
git add backend/src/main/java/com/beijvzhihou/config/
git commit -m "feat: add WebConfig, MybatisPlusConfig, RedisConfig"
```

---

### 任务 5：实体类（Entity）

**文件：**
- 创建：`backend/src/main/java/com/beijvzhihou/entity/User.java`
- 创建：`backend/src/main/java/com/beijvzhihou/entity/Post.java`
- 创建：`backend/src/main/java/com/beijvzhihou/entity/AiReply.java`
- 创建：`backend/src/main/java/com/beijvzhihou/entity/PostLike.java`
- 创建：`backend/src/main/java/com/beijvzhihou/entity/EmailCode.java`
- 创建：`backend/src/main/java/com/beijvzhihou/entity/ContentReview.java`

- [ ] **步骤 1：编写 User.java**

```java
package com.beijvzhihou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String email;           // AES加密存储原文
    private String emailMasked;    // 脱敏展示: c***a@qq.com
    private String nickname;        // 随机匿名昵称
    private String avatarUrl;       // 随机头像 URL
    private String passwordHash;   // BCrypt 加密密码
    private Integer status;         // 1=正常 0=禁用
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

- [ ] **步骤 2：编写 Post.java**

```java
package com.beijvzhihou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String content;              // 帖子内容
    private BigDecimal emotionScoreAvg;  // AI 平均情绪分
    private Integer aiResponseCount;     // AI 回复数
    private Integer likeCount;           // 点赞数缓存
    private Integer viewCount;           // 浏览数
    private Integer status;              // 1=正常 2=审核中 0=已删除
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

- [ ] **步骤 3：编写 AiReply.java**

```java
package com.beijvzhihou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_reply")
public class AiReply {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long postId;
    private String aiProvider;     // DeepSeek / Kimi2 / Qwen
    private String perspective;     // 心情 / 历史 / 技术
    private BigDecimal emotionScore; // 该AI评估的情绪分
    private String content;        // AI 回复内容
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

- [ ] **步骤 4：编写 PostLike.java**

```java
package com.beijvzhihou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post_like")
public class PostLike {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long postId;
    private Long userId;    // NULL = 匿名点赞
    private String ipHash;  // 浏览器指纹（fingerprint），用于未登录点赞去重
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

- [ ] **步骤 5：编写 EmailCode.java**

```java
package com.beijvzhihou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("email_code")
public class EmailCode {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String email;
    private String code;
    private String type;           // register / login / reset
    private LocalDateTime expiresAt;
    private Integer used;           // 0=未用 1=已用
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

- [ ] **步骤 6：编写 ContentReview.java**

```java
package com.beijvzhihou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("content_review")
public class ContentReview {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long postId;
    private String reviewType;  // keyword / ai / manual
    private String result;       // pass / reject
    private String reason;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

- [ ] **步骤 7：Commit**

```bash
git add backend/src/main/java/com/beijvzhihou/entity/
git commit -m "feat: add all entity classes (User, Post, AiReply, PostLike, EmailCode, ContentReview)"
```

---

### 任务 6：Mapper 接口

**文件：**
- 创建：`backend/src/main/java/com/beijvzhihou/mapper/UserMapper.java`
- 创建：`backend/src/main/java/com/beijvzhihou/mapper/PostMapper.java`
- 创建：`backend/src/main/java/com/beijvzhihou/mapper/AiReplyMapper.java`
- 创建：`backend/src/main/java/com/beijvzhihou/mapper/PostLikeMapper.java`
- 创建：`backend/src/main/java/com/beijvzhihou/mapper/EmailCodeMapper.java`
- 创建：`backend/src/main/java/com/beijvzhihou/mapper/ContentReviewMapper.java`

- [ ] **步骤 1：编写所有 Mapper（基于 MyBatis-Plus）**

```java
// UserMapper.java
package com.beijvzhihou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beijvzhihou.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {}

// PostMapper.java
package com.beijvzhihou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beijvzhihou.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper extends BaseMapper<Post> {}

// AiReplyMapper.java
package com.beijvzhihou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beijvzhihou.entity.AiReply;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiReplyMapper extends BaseMapper<AiReply> {}

// PostLikeMapper.java
package com.beijvzhihou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beijvzhihou.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {}

// EmailCodeMapper.java
package com.beijvzhihou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beijvzhihou.entity.EmailCode;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmailCodeMapper extends BaseMapper<EmailCode> {}

// ContentReviewMapper.java
package com.beijvzhihou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beijvzhihou.entity.ContentReview;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContentReviewMapper extends BaseMapper<ContentReview> {}
```

- [ ] **步骤 2：Commit**

```bash
git add backend/src/main/java/com/beijvzhihou/mapper/
git commit -m "feat: add all MyBatis-Plus mapper interfaces"
```

---

### 任务 7：测试验证 - 启动 Spring Boot

- [ ] **步骤 1：确保 MySQL 已启动**

验证 MySQL 服务是否运行：
```powershell
mysql -u root -p -e "SHOW DATABASES;"
```

如果报错"命令未找到"，确保 MySQL bin 目录在 PATH 中，或使用数据库工具（如 Navicat）连接。

- [ ] **步骤 2：执行数据库 SQL**

在 MySQL 客户端或 Navicat 中执行任务 0 的 SQL 语句，创建数据库和表。

- [ ] **步骤 3：启动应用（验证能否正常运行）**

```powershell
cd C:\Users\conca\.qclaw\workspace\backend
$env:Path = "E:\ai\jdk-17.0.2\bin;" + $env:Path
$env:DB_PASSWORD = ""
mvn spring-boot:run
```

预期：
- 下载依赖（首次约3-5分钟）
- 输出：`Started BeijvzhihouApplication in X seconds`
- 无红色 ERROR 日志

- [ ] **步骤 4：测试接口**

浏览器访问：http://localhost:8080/api/test/hello

预期返回：
```json
{"code":200,"message":"操作成功","data":"Hello from beijvzhihou!"}
```

- [ ] **步骤 5：Commit**

```bash
git add .
git commit -m "test: verify Spring Boot startup and API response"
```

---

## 自检清单

完成所有任务后，对照检查：

1. **编译检查：** `mvn compile` 是否通过？无红色错误？
2. **启动检查：** Spring Boot 是否正常启动？8080端口是否被占用？
3. **MySQL 检查：** 数据库 `beijvzhihou` 是否创建成功？6张表是否存在？
4. **跨域检查：** 前端 (localhost:5173) 请求后端 (localhost:8080) 是否正常？
5. **统一响应检查：** 接口返回格式是否为 `{"code":200,"message":"...","data":...}`？
6. **异常处理检查：** 抛出 `BusinessException` 是否返回正确的 JSON 错误格式？

---

*计划版本：1.0 | 创建日期：2026-04-29*
