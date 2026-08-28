# mc-authpro 项目记忆文件 (AGENTS.md)

## 项目概述

**名称**：mc-authpro  
**描述**：Paper 1.21.x 安全插件，提供账号登录、人机验证（Cloudflare Turnstile）、访问控制和黑白名单接管功能。  
**语言**：Java 21（Paper 1.21.x 要求）  
**构建工具**：Gradle 8.5（通过 Maven 生成 wrapper）  
**目标平台**：Paper 1.21.x（Java 21+ OpenJDK）  

## 核心功能

1. **账号登录**  
   - `/login <password>`：提示密码输入  
   - `/register <pwd> <pwd>`：注册新账户  
   - 密码存储为 SHA-256 + 随机盐（YAML 格式）

2. **人机验证**  
   - Cloudflare Turnstile（Managed widget，显式渲染）  
   - 一次性令牌 → 服务端 Siteverify 验证  
   - 令牌有效期 5 分钟，单次使用

3. **访问控制**  
   - 未登录/未注册 → 冻结移动/聊天/命令  
   - 登录成功后解除限制  
   - 黑白名单接管（未来扩展）

4. **实时 IP 解析**  
   - 优先级：BungeeGuard → CF-Connecting-IP → X-Forwarded-For → socket IP  
   - 记录所有出现的 IP 用于审计

5. **安全防护**  
   - 拦截方块破坏/放置  
   - 拦截玩家交互/实体交互  
   - 拦截背包操作  
   - 阻止 F3+F4 游戏模式切换  
   - OP 权限检查（无视 OP 状态）  
   - 防 DoS：监控区块加载，强制 view distance=2  
   - 验证室系统：未验证玩家传送到隔离区域

## 技术栈

| 层级 | 技术 | 版本/说明 |
|------|------|-----------|
| 语言 | Java | 21 (OpenJDK 25) |
| 构建 | Gradle | 8.5 |
| 框架 | Paper | 1.21.x |
| 存储 | YAML | players/<uuid>.yml |
| 哈希 | SHA-256 + 随机盐 | 密码存储 |
| 验证 | Cloudflare Turnstile | Siteverify API |
| 网络 | Java HttpClient | 异步 POST 调用 |
| UI | Chat + Command | 命令交互 |

## 配置文件

### config.yml（核心节选）
```yaml
verification:
  enabled: true
  static-site-base-url: "https://wzml.cc.cd/mc-authpro"
  siteverify-url: "https://challenges.cloudflare.com/turnstile/v0/siteverify"
  secret-key: "1x0000000000000000000000000000000AA"  # 测试用
  action: "mc-login"
  token-ttl-seconds: 300
  timeout-seconds: 300
  expected-hostname: ""
  http-timeout-seconds: 10

session:
  timeout-seconds: 180
  remember-ip-minutes: 30

real-ip:
  trust-proxy: true
  cf-header: "CF-Connecting-IP"
  forwarded-header: "X-Forwarded-For"
  bungee-guard: true
  max-ips-to-record: 5

security:
  max-login-attempts: 5
  lockout-seconds: 60
  allow-multiple-logins-per-ip: true

verification-chamber:
  enabled: true
  world: "world"
  x: 0
  y: 100
  z: 0
  force-spectator: true
  max-view-distance: 2

anti-dos:
  max-concurrent-unauth: 5
  verification-timeout: 300
```

## 目录结构

```
mc-authpro/
├── src/main/java/com/example/mcauthpro/
│   ├── McAuthPro.java                 # 主类
│   ├── command/                      # 命令类
│   │   ├── LoginCommand.java
│   │   ├── RegisterCommand.java
│   │   ├── VerifyCommand.java        # 核心：验证令牌
│   │   ├── LogoutCommand.java
│   │   └── AuthAdminCommand.java
│   ├── listener/                     # 监听器
│   │   ├── PlayerJoinListener.java   # 验证室传送
│   │   ├── PlayerMoveListener.java
│   │   ├── PlayerChatListener.java
│   │   ├── PlayerCommandListener.java
│   │   └── PlayerQuitListener.java
│   │   ├── BlockBreakListener.java   # ★ 拦截方块破坏
│   │   ├── BlockPlaceListener.java   # ★ 拦截方块放置
│   │   ├── PlayerInteractListener.java # ★ 拦截交互
│   │   ├── PlayerInteractEntityListener.java # ★ 拦截实体交互
│   │   ├── InventoryClickListener.java # ★ 拦截背包
│   │   ├── ChunkLoadListener.java    # ★ 监控区块加载
│   │   └── GameModeChangeListener.java # ★ 阻止F3+F4
│   ├── auth/                         # 认证相关
│   │   ├── AuthService.java
│   │   ├── PasswordHasher.java
│   │   ├── SessionManager.java
│   │   ├── PlayerData.java
│   │   └── StorageService.java
│   ├── network/                      # 网络相关
│   │   ├── RealIpResolver.java
│   │   ├── VerificationSite.java
│   │   └── TurnstileValidator.java   # Siteverify 调用
│   ├── gui/                          # GUI 验证页面
│   │   ├── VerificationBook.java
│   │   └── BossBarHandler.java
│   ├── config/                      # 配置
│   │   ├── PluginConfig.java
│   │   └── Messages.java
│   └── util/                        # 工具类
│       ├── HttpUtil.java
│       └── TimeUtil.java
├── resources/
│   ├── plugin.yml
│   ├── config.yml
│   ├── messages.yml
│   └── web/
│       └── index.html               # 静态验证页面
└── gradle/                          # 构建配置
    ├── settings.gradle.kts
    ├── build.gradle.kts
    └── gradle.properties
```

## 环境要求

- **Java**：OpenJDK 21+（已安装，版本 25.0.4）
- **Maven**：3.9.9（已安装）
- **Gradle**：8.5（已安装到 /home/wayne/.gradle/gradle-8.5）
- **Git**：已初始化（.git 目录存在）
- **网络**：可访问 GitHub（用于镜像下载）

## 构建流程

1. `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`
2. `./gradlew build` —— 编译并生成 JAR
3. JAR 输出位置：`build/libs/mc-authpro-0.1.0.jar`
4. 部署到 Paper 服务器：`cp build/libs/mc-authpro-0.1.0.jar /media/wayne/Elements/gamestamp/mc_server/plugins/`

## 测试环境

- **服务器路径**：`/media/wayne/Elements/gamestamp/mc_server/`
- **Paper 版本**：26.2-119（Minecraft 26.2）
- **插件加载**：已验证，McAuthPro v0.1.0 已成功加载
- **GitHub Pages**：https://wzml.cc.cd/mc-authpro

## 国内镜像源配置

- **Gradle 发行版**：`https://mirrors.cloud.tencent.com/gradle/gradle-8.5-bin.zip`
- **Maven 仓库**：`https://maven.aliyun.com/repository/public`
- **Paper 仓库**：`https://repo.papermc.io/repository/maven-public/`

## 后续任务

1. 实现完整的 Cloudflare Turnstile 测试流程
2. 添加静态页面到 web 服务器目录
3. 实现黑白名单模块
4. 添加 GUI 验证界面（Book + BossBar）
5. 完成 Git 提交流程

## 注意事项

- **令牌安全**：Secret Key 必须放在环境变量或配置文件中，不要硬编码
- **IP 记录**：所有访问尝试的 IP 都会被记录，用于审计
- **令牌生命周期**：5 分钟有效，单次使用，防止重放攻击
- **灰度发布**：先在测试环境验证，再逐步上线

## 版本历史

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-08-28 | 初版，验证模块和持久化模块实现 |
| 0.1.1 | 2026-08-28 | 添加安全防护和验证室系统 |
| 0.2.0 | 待定 | 添加黑白名单功能 |

## 联系人

- 项目负责人：Wayne
- 开发语言：Java 21
- 构建工具：Gradle 8.5

*本文件是项目的唯一记忆源，所有修改均需同步到此文件。*
