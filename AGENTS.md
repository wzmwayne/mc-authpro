# mc-authpro 项目记忆文件 (AGENTS.md)

## 项目概述

**名称**：mc-authpro  
**描述**：Paper 1.21.x 安全插件，提供账号登录、人机验证（Cloudflare Turnstile）、访问控制。  
**语言**：Java 21（Paper 1.21.x 要求）  
**构建工具**：Gradle 8.5（通过 Maven 生成 wrapper）  
**目标平台**：Paper 1.21.x（Java 21+ OpenJDK）  
**当前版本**：0.2.1  

## 核心功能

1. **加入即人机验证**  
   - 玩家加入 → 立即传送至login世界 → 生成 8 位 hex 单次 key → 显示验证 URL + 30秒倒计时标题  
   - `/verify <会话ID> <令牌>` → 验证通过  
   - 超时/未验证 → 踢出

2. **单次验证 key 机制**  
   - 服务器生成 8 位 16 进制 key（如 `a3f8b2c1`），查重确保唯一  
   - key 映射到玩家名，验证成功/超时/退出时自动失效  
   - 网页零校验，只读参数渲染 Turnstile 组件

3. **账号登录/注册**  
   - `/register <密码> <密码>` / `/reg <密码> <密码>`：注册新账户  
   - `/login <密码>`：登录  
   - `/changepwd <旧密码> <新密码>`：修改密码  
   - 密码存储为 SHA-256 + 随机盐（YAML 格式）

4. **顶号保护**  
   - 登录成功后扫描所有世界中的同名玩家  
   - 若 world 世界存在同名 → 踢出 world 玩家（"你不是本人请改密"）  
   - 登录者显示欢迎信息

5. **登录世界保护**  
   - VoidChunkGenerator 虚空世界  
   - 屏障平台 + 围墙  
   - LoginWorldListener 全面禁止方块破坏/放置/交互/伤害/移动/背包操作  
   - 同名检测：login 世界已有同名 → 踢出后来者

6. **访问控制**  
   - 未完成验证 → 冻结移动/聊天/命令  
   - 登录成功后解除限制，传送到上次退出位置  

7. **配置/消息自动补全**  
   - 启动时对比 jar 内默认 config.yml，缺失 key 自动补全  
   - 启动时对比 jar 内默认 messages.yml，缺失 key 自动补全

8. **控制台横幅**  
   - 启动时输出 AuthPro Unicode 艺术字  
   - 自检报告：服务器信息、在线模式、玩家上限、世界数量、登录世界状态、验证配置、已注册玩家数、会话统计

9. **自动关闭在线模式**  
   - 启动时自动修改 server.properties，将 online-mode 设为 false  
   - 可通过 `verification.auto-disable-online-mode: false` 跳过

## 技术栈

| 层级 | 技术 | 版本/说明 |
|------|------|-----------|
| 语言 | Java | 21 (OpenJDK 25) |
| 构建 | Gradle | 8.5 |
| 框架 | Paper | 1.21.x |
| 存储 | YAML | players/<玩家名>.yml |
| 哈希 | SHA-256 + 随机盐 | 密码存储 |
| 验证 | Cloudflare Turnstile | Siteverify API |
| 网络 | Java HttpClient | POST 调用 |

## Session 系统（按玩家名索引）

```
SessionManager 使用 Set<String> 按玩家名追踪状态：
- turnstileVerified: Set<String> → 已通过人机验证的玩家名
- loginVerified: Set<String> → 已完成登录的玩家名
- countdowns: Map<String, Integer> → 倒计时追踪
- pendingKeys: Map<String, String> → 8位hex key → 玩家名映射

生命周期：
玩家加入 → createVerificationKey() → 生成 8位hex key
→ /verify <key> <token> → markTurnstileVerified
→ /login /reg → markLoginVerified → isFullyAuthenticated = true
→ 玩家退出 → remove（清理所有状态 + pendingKeys）
```

## 玩家名校验规则

- 仅允许纯字母（大小写）+ 数字
- 区分大小写（`Steve` ≠ `steve`）
- 重名检查使用 `equals()`（大小写敏感）
- 非法玩家名直接踢出

## 玩家数据存储

- 文件路径：`plugins/McAuthPro/players/<玩家名>.yml`
- 不再使用 UUID 作为文件标识
- 包含字段：username, password-hash, salt, ip-history, registered-at, last-location

## 验证网页

- 地址：`https://wzml.cc.cd/mc-authpro`
- URL 格式：`{base-url}?session={8位hex key}&sitekey={cf-sitekey}`
- 网页零校验，只读参数渲染 Turnstile 组件
- 验证成功后自动拼接 `/verify {key} {token}` 并复制到剪贴板

## 配置文件

### config.yml（核心节选）
```yaml
verification:
  enabled: true
  auto-disable-online-mode: true
  static-site-base-url: "https://wzml.cc.cd/mc-authpro"
  siteverify-url: "https://challenges.cloudflare.com/turnstile/v0/siteverify"
  secret-key: "1x0000000000000000000000000000000AA"
  sitekey: "1x00000000000000000000AA"
  action: "mc-login"
  token-ttl-seconds: 300
  timeout-seconds: 300
  expected-hostname: ""
  http-timeout-seconds: 10
  countdown-seconds: 30

login-world:
  enabled: true
  name: "login"
  platform-y: 100
  platform-size: 2
  wall-height: 1
  force-spectator: true
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
│   ├── McAuthPro.java                 # 主类（横幅 + 自检 + 关闭在线模式）
│   ├── command/                      # 命令类
│   │   ├── LoginCommand.java
│   │   ├── RegisterCommand.java
│   │   ├── VerifyCommand.java        # /verify <key> <token>
│   │   ├── ChangePasswordCommand.java # /changepwd
│   │   └── AuthAdminCommand.java     # /authadmin rebuildlogin
│   ├── listener/                     # 监听器
│   │   ├── PlayerJoinListener.java   # 加入即传login世界 + 重名检查 + 名校验
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
│   │   ├── AuthService.java          # isFullyAuthenticated + changePassword + saveLastLocation + isValidPlayerName
│   │   ├── PasswordHasher.java
│   │   ├── SessionManager.java       # 按玩家名索引 + pendingKeys
│   │   ├── PlayerData.java           # 含 last-location 字段（无 uuid）
│   │   └── StorageService.java       # 读写YAML + 玩家名文件
│   ├── network/                      # 网络相关
│   │   ├── RealIpResolver.java
│   │   ├── VerificationSite.java     # URL 拼接 key + sitekey
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
│       └── index.html               # 静态验证页面（零校验 + 自动复制）
└── gradle/
    ├── settings.gradle.kts
    ├── build.gradle.kts
    └── gradle.properties
```

## 完整验证流程

```
玩家加入服务器
  ↓
检查 login 世界是否已有同名玩家（大小写敏感）
  ├─ 有 → 踢出后来者："你已有一个会话在进行中"
  └─ 无 → 玩家名校验（纯字母+数字）
              ↓
          传送至 login 世界 + 旁观者模式
              ↓
          生成 8 位 hex key（查重）→ pendingKeys[key] = 玩家名
              ↓
          显示 URL：{base-url}?session={key}&sitekey={cf-sitekey}
          标题倒计时（默认30秒）
              ↓
          玩家打开链接 → 网页读取参数 → 渲染 Turnstile（零校验）
              ↓
          验证完成 → 自动复制：/verify {key} {token}
              ↓
玩家执行 /verify {key} {token}
  ↓
      服务器查 key → 验证玩家名匹配
              ↓
          调 Cloudflare Siteverify API
              ↓
          标记 turnstileVerified
              ↓
          已注册？→ 标题提示 "请通过 /login 登陆"
          未注册？→ 标题提示 "请通过 /reg 注册"
              ↓
          /reg 成功 → markLoginVerified → 提示 /login
              ↓
          /login 成功 → markLoginVerified
              ↓
          标题显示 "密码正确，准备进入……" + "扫描账户中"（3秒）
              ↓
          扫描所有世界中的同名玩家（大小写敏感）
          ├─ 有 → 踢出 world 玩家（顶号警告 + 改密提示）
          └─ 无 → 继续
              ↓
          标题 "进入服务器" + "欢迎回来，{player}！"（3秒）
              ↓
          传送到上次退出位置（或出生地）
              ↓
          GameMode.SURVIVAL → 解除所有限制 → 正常游戏
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
3. JAR 输出位置：`build/libs/mc-authpro-0.2.1.jar`
4. 部署到 Paper 服务器：`cp build/libs/mc-authpro-0.2.1.jar <server>/plugins/`

## 测试环境

- **服务器路径**：`/media/wayne/Elements/gamestamp/mc_server/`
- **Paper 版本**：26.2-120（Minecraft 26.2）
- **插件加载**：已验证，McAuthPro v0.2.1 已成功加载
- **GitHub Pages**：https://wzml.cc.cd/mc-authpro

## 国内镜像源配置

- **Gradle 发行版**：`https://mirrors.cloud.tencent.com/gradle/gradle-8.5-bin.zip`
- **Maven 仓库**：`https://maven.aliyun.com/repository/public`
- **Paper 仓库**：`https://repo.papermc.io/repository/maven-public/`

## 版本历史

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-08-28 | 初版，验证模块和持久化模块实现 |
| 0.1.1 | 2026-08-28 | 添加安全防护和验证室系统 |
| 0.2.0 | 2026-08-29 | 重构为玩家名索引 + 专用login世界 + 顶号保护 + 改密命令 |
| 0.2.1 | 2026-08-29 | 单次8位hex key + 网页零校验 + 玩家名标记 + 大小写敏感 + 名校验 |

## 注意事项

- **令牌安全**：Secret Key 必须放在环境变量或配置文件中，不要硬编码
- **IP 记录**：所有访问尝试的 IP 都会被记录，用于审计
- **位置保存**：退出时自动保存玩家位置，下次登录恢复
- **玩家名索引**：Session 按玩家名追踪，大小写敏感
- **顶号警告**：world 世界玩家被踢时提示改密，防止盗号
- **玩家名校验**：仅允许纯字母（大小写）+ 数字，非法玩家名直接踢出

## 联系人

- 项目负责人：Wayne
- 开发语言：Java 21
- 构建工具：Gradle 8.5

*本文件是项目的唯一记忆源，所有修改均需同步到此文件。*
