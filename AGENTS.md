# mc-authpro 项目记忆文件 (AGENTS.md)

## 项目概述

**名称**：mc-authpro  
**描述**：Paper 1.21.x 安全插件，提供账号登录、人机验证（Cloudflare Turnstile）、访问控制和黑白名单接管功能。  
**语言**：Java 21（Paper 1.21.x 要求）  
**构建工具**：Gradle 8.5（通过 Maven 生成 wrapper）  
**目标平台**：Paper 1.21.x（Java 21+ OpenJDK）  

## 核心功能

1. **加入即人机验证**  
   - 玩家加入 → 立即传送至login世界 → 显示Turnstile URL + 30秒倒计时标题  
   - `/verify <令牌>` → 验证通过  
   - 超时/未验证 → 踢出

2. **账号登录/注册**  
   - `/register <密码> <密码>`：注册新账户  
   - `/login <密码>`：登录  
   - `/changepwd <旧密码> <新密码>`：修改密码  
   - 密码存储为 SHA-256 + 随机盐（YAML 格式）

3. **顶号保护**  
   - 登录成功后扫描所有世界中的同名玩家  
   - 若world世界存在同名 → 踢出world玩家（"你不是本人请改密"）  
   - 登录者显示欢迎信息

4. **登录世界保护**  
   - VoidChunkGenerator 虚空世界  
   - 屏障平台 + 围墙  
   - LoginWorldListener 全面禁止方块破坏/放置/交互/伤害/移动/背包操作  
   - 同名检测：login世界已有同名 → 踢出后来者

5. **访问控制**  
   - 未完成验证 → 冻结移动/聊天/命令  
   - 登录成功后解除限制，传送到上次退出位置  
   - 黑白名单接管（未来扩展）

6. **实时 IP 解析**  
   - 优先级：BungeeGuard → CF-Connecting-IP → X-Forwarded-For → socket IP  
   - 记录所有出现的 IP 用于审计

7. **配置/消息自动补全**  
   - 启动时对比 jar 内默认 config.yml，缺失 key 自动补全  
   - 启动时对比 jar 内默认 messages.yml，缺失 key 自动补全

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

## Session 系统（按玩家名索引）

```
SessionManager 使用 Set<String> 按玩家名追踪状态：
- turnstileVerified: Set<String> → 已通过人机验证的玩家名
- loginVerified: Set<String> → 已完成登录的玩家名
- countdowns: Map<String, Integer> → 倒计时追踪

生命周期：
玩家加入 → createSession → /verify → markTurnstileVerified
→ /login → markLoginVerified → isFullyAuthenticated = true
→ 玩家退出 → remove（清理所有状态）
```

## 配置文件

### config.yml（核心节选）
```yaml
verification:
  enabled: true
  static-site-base-url: "https://wzml.cc.cd/mc-authpro"
  siteverify-url: "https://challenges.cloudflare.com/turnstile/v0/siteverify"
  secret-key: "1x0000000000000000000000000000000AA"
  action: "mc-login"
  token-ttl-seconds: 300
  timeout-seconds: 300
  expected-hostname: ""
  http-timeout-seconds: 10
  countdown-seconds: 30

session:
  timeout-seconds: 180
  remember-ip-minutes: 30

login-world:
  enabled: true
  name: "login"
  platform-y: 100
  platform-size: 2
  wall-height: 1
```

## 消息配置

`messages.yml` 与 `config.yml` 同目录，所有玩家可见文字均可配置：
- 支持 `&a` `&c` 等颜色代码
- 支持 `{player}` `{url}` `{seconds}` 占位符
- 支持多行字符串（YAML `|` 语法）
- 启动时自动补全缺失项

## 目录结构

```
mc-authpro/
├── src/main/java/com/example/mcauthpro/
│   ├── McAuthPro.java                 # 主类
│   ├── command/                      # 命令类
│   │   ├── LoginCommand.java
│   │   ├── RegisterCommand.java
│   │   ├── VerifyCommand.java        # 核心：验证令牌
│   │   ├── ChangePasswordCommand.java # /changepwd
│   │   └── AuthAdminCommand.java     # /authadmin rebuildlogin
│   ├── listener/                     # 监听器
│   │   ├── PlayerJoinListener.java   # 加入即传login世界 + 重名检查
│   │   ├── PlayerQuitListener.java   # 退出时保存位置 + 清理session
│   │   ├── PlayerMoveListener.java   # 冻结移动
│   │   ├── PlayerChatListener.java   # 冻结聊天 + 区分注册状态提示
│   │   ├── PlayerCommandListener.java # 允许 /login /reg /verify /changepwd
│   │   ├── BlockBreakListener.java   # 拦截方块破坏
│   │   ├── BlockPlaceListener.java   # 拦截方块放置
│   │   ├── PlayerInteractListener.java # 拦截交互
│   │   ├── PlayerInteractEntityListener.java # 拦截实体交互
│   │   ├── InventoryClickListener.java # 拦截背包
│   │   ├── ChunkLoadListener.java    # 监控区块加载
│   │   ├── GameModeChangeListener.java # 阻止F3+F4
│   │   └── LoginWorldListener.java   # ★ login世界全面保护
│   ├── auth/                         # 认证相关
│   │   ├── AuthService.java          # isFullyAuthenticated + changePassword + saveLastLocation
│   │   ├── PasswordHasher.java
│   │   ├── SessionManager.java       # 按玩家名索引
│   │   ├── PlayerData.java           # 含 last-location 字段
│   │   └── StorageService.java       # 读写YAML + last-location
│   ├── network/                      # 网络相关
│   │   ├── RealIpResolver.java
│   │   ├── VerificationSite.java
│   │   └── TurnstileValidator.java   # Siteverify 调用
│   ├── config/                       # 配置
│   │   ├── PluginConfig.java         # 自动补全缺失 config key
│   │   └── Messages.java             # 自动补全缺失 messages key
│   └── world/                        # 登录世界
│       ├── LoginWorldManager.java    # 创建/重构 login 世界
│       └── VoidChunkGenerator.java   # 虚空区块生成器
├── resources/
│   ├── plugin.yml
│   ├── config.yml
│   ├── messages.yml
│   └── web/
│       └── index.html               # 静态验证页面（显示完整 /verify 命令）
└── gradle/
    ├── settings.gradle.kts
    ├── build.gradle.kts
    └── gradle.properties
```

## 完整验证流程

```
玩家加入服务器
  ↓
检查 login 世界是否已有同名玩家
  ├─ 有 → 踢出后来者："你已有一个会话在进行中"
  └─ 无 → 传送至 login 世界 + 旁观者模式
              ↓
          显示 Turnstile URL + 标题倒计时（默认30秒）
              ↓
          /verify <令牌> → markTurnstileVerified
              ↓
          已注册？→ 标题提示 "请通过 /login 登陆"
          未注册？→ 标题提示 "请通过 /reg 注册"
              ↓
          /reg 成功 → markLoginVerified → 提示 /login
              ↓
          /login 成功 → markLoginVerified
              ↓
          标题显示 "密码正确，准备进入" + "扫描账户中"（3秒）
              ↓
          扫描所有世界中的同名玩家
          ├─ 有 → 踢出 world 玩家（顶号警告 + 改密提示）
          └─ 无 → 直接传送
              ↓
          传送到上次退出位置（或出生地）
              ↓
          解除所有限制 → 正常游戏
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

1. 实现黑白名单模块
2. 添加 GUI 验证界面（Book + BossBar）
3. 完成 Git 提交流程

## 注意事项

- **令牌安全**：Secret Key 必须放在环境变量或配置文件中，不要硬编码
- **IP 记录**：所有访问尝试的 IP 都会被记录，用于审计
- **令牌生命周期**：5 分钟有效，单次使用，防止重放攻击
- **位置保存**：退出时自动保存玩家位置，下次登录恢复
- **玩家名索引**：Session 按玩家名追踪，注意重名冲突
- **顶号警告**：world 世界玩家被踢时提示改密，防止盗号

## 版本历史

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-08-28 | 初版，验证模块和持久化模块实现 |
| 0.1.1 | 2026-08-28 | 添加安全防护和验证室系统 |
| 0.2.0 | 2026-08-29 | 重构为玩家名索引 + 专用login世界 + 顶号保护 + 改密命令 |

## 联系人

- 项目负责人：Wayne
- 开发语言：Java 21
- 构建工具：Gradle 8.5

*本文件是项目的唯一记忆源，所有修改均需同步到此文件。*
