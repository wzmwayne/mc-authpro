# McAuthPro

Paper 1.21.x 安全插件，提供账号登录、Cloudflare Turnstile 人机验证和访问控制。

## 功能特性

- **加入即人机验证**：玩家加入服务器后立即传送至专用登录世界，要求完成 Turnstile 验证
- **单次验证 key**：8 位 hex 临时 key，验证成功或超时后自动失效
- **账号注册/登录**：SHA-256 + 随机盐密码存储，支持注册、登录、修改密码
- **顶号保护**：登录成功后扫描所有世界，踢出同名玩家并提示改密
- **登录世界保护**：专用虚空世界 + 屏障平台，全面禁止方块破坏/放置/交互/伤害
- **访问控制**：未验证玩家冻结移动、聊天、命令，验证完成后解除限制
- **自动关闭在线模式**：启动时自动修改 `server.properties` 将 `online-mode` 设为 `false`
- **配置/消息自动补全**：启动时对比 jar 内默认配置，缺失项自动补全

## 验证流程

```
玩家加入 → 生成 8 位 hex key → 传送至 login 世界
    ↓
显示验证 URL（含 key + sitekey）→ 30 秒倒计时
    ↓
玩家打开链接 → 完成 Turnstile → 自动复制 /verify {key} {token}
    ↓
粘贴发送 → 服务端验证 token → 标记验证通过
    ↓
已注册 → 提示 /login    未注册 → 提示 /reg
    ↓
登录/注册成功 → 扫描 world 重名 → 传送至上次退出位置
```

## 安装

1. 确保服务器运行 Paper 1.21.x（Java 21+）
2. 将 `mc-authpro-0.2.0.jar` 放入 `plugins/` 目录
3. 启动服务器，插件会自动生成 `config.yml` 和 `messages.yml`
4. 修改 `config.yml` 中的 `verification.secret-key` 和 `verification.sitekey`
5. 重启服务器生效

## 配置

### config.yml

```yaml
verification:
  enabled: true
  auto-disable-online-mode: true
  static-site-base-url: "https://wzml.cc.cd/mc-authpro"
  siteverify-url: "https://challenges.cloudflare.com/turnstile/v0/siteverify"
  secret-key: "1x0000000000000000000000000000000AA"   # Cloudflare 后端密钥
  sitekey: "1x00000000000000000000AA"                  # Cloudflare 前端站点密钥
  action: "mc-login"
  token-ttl-seconds: 300
  timeout-seconds: 300
  countdown-seconds: 30

login-world:
  enabled: true
  name: "login"
  platform-y: 100
  platform-size: 2
  wall-height: 1
  force-spectator: true
```

### messages.yml

所有玩家可见文字均可在此修改，支持 `&` 颜色代码和 `{player}` `{url}` 占位符。

## 命令

| 命令 | 说明 | 权限 |
|------|------|------|
| `/login <密码>` | 登录账号 | `mcauthpro.login`（默认） |
| `/register <密码> <密码>` | 注册新账号 | `mcauthpro.register`（默认） |
| `/reg <密码> <密码>` | 注册别名 | `mcauthpro.register`（默认） |
| `/verify <会话ID> <令牌>` | 验证 Turnstile | `mcauthpro.verify`（默认） |
| `/changepwd <旧密码> <新密码>` | 修改密码 | `mcauthpro.changepassword`（默认） |
| `/authadmin rebuildlogin` | 重构登录世界 | `mcauthpro.admin`（OP） |

## 玩家数据

玩家数据存储在 `plugins/McAuthPro/players/<uuid>.yml`：

```yaml
username: "Steve"
password-hash: "..."
salt: "..."
ip-history: [...]
registered-at: 1690000000000
last-location:
  world: "world"
  x: 100.5
  y: 64.0
  z: -30.5
  yaw: 180.0
  pitch: 0.0
```

## 构建

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
./gradlew build
```

输出：`build/libs/mc-authpro-0.2.0.jar`

## 技术栈

| 层级 | 技术 |
|------|------|
| 语言 | Java 21 |
| 构建 | Gradle 8.5 |
| 框架 | Paper 1.21.x |
| 存储 | YAML |
| 哈希 | SHA-256 + 随机盐 |
| 验证 | Cloudflare Turnstile |

## 注意事项

- 服务端必须设置 `online-mode=false`
- Turnstile 密钥需在 [Cloudflare Dashboard](https://dash.cloudflare.com/) 创建
- 验证网页需部署到支持 HTTPS 的静态托管（如 GitHub Pages）

## License

MIT
