# JXC 进销存管理系统 - 免费部署指南

全程无需信用卡，使用三个免费平台完成上线。

---

## 架构总览

```
用户浏览器
   ↓
GitHub Pages (前端 Vue 3 静态站)  ← 免费
   ↓ API 请求
Koyeb (后端 Spring Boot)         ← 免费 nano 实例
   ↓ 数据库连接
TiDB Cloud Serverless (MySQL)    ← 免费 5GB
```

---

## 第1步：注册账号

在开始之前，请先注册以下账号（均无需信用卡）：

1. **GitHub**：https://github.com （如已有账号跳过）
2. **TiDB Cloud**：https://tidbcloud.com 用 GitHub 账号注册
3. **Koyeb**：https://koyeb.com 用 GitHub 账号注册

---

## 第2步：创建 TiDB Cloud 数据库

1. 登录 TiDB Cloud 控制台
2. 点击 **Create Cluster** → 选择 **Serverless**
3. 集群名称填 `jxc`，选择离你近的区域（如 `Virginia (us-east-1)`）
4. 点击 **Create**，等待创建完成（约1分钟）

### 初始化数据库

1. 在集群详情页，点击 **Connect** 按钮
2. 复制连接信息（稍后要用）
3. 点击 **Chat2Query**（SQL 编辑器）
4. 把项目根目录的 `init.sql` 文件内容粘贴进去，执行

### 记录连接信息

连接信息格式如下，记录下来：

```
Host:     gateway01.us-east-1.prod.aws.tidbcloud.com
Port:     4000
User:     xxxxx.root
Password: 你设置的密码
Database: test
```

对应的 JDBC URL：

```
jdbc:mysql://gateway01.us-east-1.prod.aws.tidbcloud.com:4000/test?useSSL=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true
```

> 注意：User 带有 `.root` 后缀，如 `2ABC123.root`

---

## 第3步：部署后端到 Koyeb

1. 登录 Koyeb 控制台：https://app.koyeb.com
2. 点击 **Create App** → **Docker** → **GitHub**
3. 授权 Koyeb 访问你的 GitHub 仓库，选择 `jxc-system` 仓库
4. 配置构建：
   - **Dockerfile location**: `Dockerfile.backend`
   - **Instance type**: **Nano**（免费）
5. 配置环境变量（Secrets）：

| 变量名 | 值 |
|--------|-----|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SERVER_PORT` | `8080` |
| `JAVA_OPTS` | `-Xms128m -Xmx192m` |
| `DB_URL` | TiDB 的 JDBC URL（见第2步） |
| `DB_USERNAME` | TiDB 的用户名（如 `2ABC123.root`） |
| `DB_PASSWORD` | TiDB 的密码 |
| `JWT_SECRET` | 随机生成的 64 位字符串 |
| `CORS_ORIGINS` | `https://你的github用户名.github.io` |

6. 端口设置：`8080`，协议 `HTTP`
7. 点击 **Deploy**

部署完成后，Koyeb 会分配一个域名，类似：
```
https://jxc-backend-你的账号.koyeb.app
```

记录这个域名，下一步要用。

---

## 第4步：部署前端到 GitHub Pages

### 4.1 推送代码到 GitHub

```bash
# 在项目目录执行
git init
git add .
git commit -m "Initial commit for deployment"
git remote add origin https://github.com/你的用户名/jxc-system.git
git push -u origin main
```

### 4.2 配置 GitHub 仓库

1. 进入仓库 → **Settings** → **Pages**
2. Source 选择 **GitHub Actions**

### 4.3 配置前端 API 地址

1. 进入仓库 → **Settings** → **Secrets and variables** → **Actions** → **Variables** 标签
2. 点击 **New repository variable**
3. 添加：

| Name | Value |
|------|-------|
| `VITE_API_BASE_URL` | `https://jxc-backend-你的账号.koyeb.app/api` |

> 替换为你第3步记录的 Koyeb 域名

### 4.4 触发部署

```bash
# 修改任意文件触发 Actions
git commit --allow-empty -m "trigger deploy"
git push
```

GitHub Actions 会自动构建并部署前端到 GitHub Pages。

部署完成后，访问地址：
```
https://你的github用户名.github.io/jxc-system/
```

---

## 第5步：更新 CORS 配置

前端部署完成后，回到 Koyeb 控制台，更新后端的 `CORS_ORIGINS` 环境变量：

```
CORS_ORIGINS=https://你的github用户名.github.io
```

Koyeb 会自动重新部署。

---

## 验证部署

1. 打开 `https://你的github用户名.github.io/jxc-system/`
2. 应该能看到登录页面
3. 使用数据库中的账号登录测试

---

## 常见问题

### 前端白屏
- 检查 GitHub Actions 构建日志是否成功
- 确认 `VITE_API_BASE_URL` 变量设置正确

### API 请求失败（CORS 错误）
- 确认 Koyeb 的 `CORS_ORIGINS` 包含你的 GitHub Pages 域名
- 域名格式：`https://你的github用户名.github.io`（不要带路径）

### 后端启动失败
- 检查 Koyeb 日志（控制台 → Logs 标签）
- 确认 TiDB 连接信息正确
- 确认 `DB_URL` 包含 `useSSL=true`

### 数据库连接超时
- TiDB Cloud Serverless 可能有冷启动，首次连接较慢
- 等待几秒后重试

### 后端冷启动慢
- Koyeb 免费层在无请求时会休眠
- 首次访问需等待 30 秒左右
- 可用 UptimeRobot 免费监控保活（每5分钟 ping 一次）

---

## 免费额度说明

| 平台 | 免费额度 | 限制 |
|------|---------|------|
| GitHub Pages | 无限 | 仓库 < 1GB，每月 100GB 流量 |
| Koyeb | 1 个 nano 实例 | 无休眠，512MB 内存 |
| TiDB Cloud | 5GB 存储 | 50M 请求单元/月 |
| GitHub Actions | 2000 分钟/月 | 公开仓库不限 |

---

## 保活设置（可选）

Koyeb 免费层可能因长期无请求休眠，可用免费监控保活：

1. 注册 [UptimeRobot](https://uptimerobot.com)（免费）
2. 添加 HTTP(s) 监控
3. URL 填：`https://你的koyeb域名.koyeb.app/actuator/health`
4. 间隔设为 5 分钟
