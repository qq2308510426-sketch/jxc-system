# JXC 进销存管理系统 - 部署指南

## 方案一：GitHub + Railway（推荐，最简单）

全程免费，自动部署，无需服务器。

### 第1步：推送到 GitHub

```bash
# 在项目目录执行
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/你的用户名/jxc-system.git
git push -u origin main
```

### 第2步：部署后端到 Railway

1. 打开 [Railway.app](https://railway.app)（用 GitHub 登录）
2. 点击 **New Project** → **Deploy from GitHub repo**
3. 选择你的 `jxc-system` 仓库
4. 选择 **Dockerfile** 构建，路径填 `Dockerfile.backend`
5. 添加环境变量：
   - `DB_PASSWORD` = 你的密码
   - `JWT_SECRET` = 随机长字符串（32位以上）
   - `SPRING_PROFILES_ACTIVE` = prod
   - `SERVER_PORT` = 8080
6. 添加 MySQL 数据库：**New** → **Database** → **MySQL**
7. 复制 MySQL 的 `DATABASE_URL`，设置为 `DB_URL` 环境变量

### 第3步：部署前端到 Railway

1. 在同一个项目中 **New** → **GitHub Repo**
2. 选择 Dockerfile 构建，路径填 `Dockerfile.frontend`
3. 设置环境变量：
   - `BACKEND_HOST` = 后端服务的内部地址
4. Railway 会自动分配域名（如 `xxx.up.railway.app`）

### 第4步：配置自定义域名（可选）

1. Railway 项目设置 → **Custom Domain**
2. 输入你的域名
3. 按提示添加 DNS 记录
4. SSL 自动配置（免费）

---

## 方案二：GitHub Actions + 自己的服务器

适合有云服务器的用户。

### 第1步：推送到 GitHub

同方案一。

### 第2步：配置 GitHub Secrets

进入 GitHub 仓库 → **Settings** → **Secrets and variables** → **Actions**：

| Secret | 说明 |
|--------|------|
| `SERVER_HOST` | 服务器 IP |
| `SERVER_USER` | SSH 用户名（如 ubuntu） |
| `SSH_KEY` | SSH 私钥内容 |

### 第3步：服务器初始化（一次性）

```bash
# 在服务器上执行
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
git clone https://github.com/你的用户名/jxc-system.git /opt/jxc
cd /opt/jxc
cp .env.example .env
vim .env  # 填入真实密码
chmod +x deploy.sh
./deploy.sh
```

### 第4步：自动部署

之后每次 `git push main`，GitHub Actions 会自动：
1. 构建前端和后端
2. 通过 SSH 部署到服务器
3. 自动重启服务

---

## 方案三：GitHub Pages（仅前端）+ Railway（后端）

如果想用 GitHub Pages 托管前端：

1. 前端推送到 `gh-pages` 分支
2. GitHub 仓库设置 → **Pages** → 选择 `gh-pages` 分支
3. 前端访问地址：`https://你的用户名.github.io/jxc-system/`
4. 后端仍然部署在 Railway

---

## 免费资源清单

| 资源 | 方案 | 说明 |
|------|------|------|
| 代码托管 | GitHub | 免费私有/公开仓库 |
| 前端+后端部署 | Railway | 免费 $5/月额度，够用 |
| 数据库 | Railway MySQL | 免费包含在 Railway |
| SSL 证书 | Railway 自动 | 自动 HTTPS |
| 自定义域名 | Railway | 免费绑定域名 |
| CI/CD | GitHub Actions | 免费 2000分钟/月 |
| 监控 | UptimeRobot | 免费 50 个监控 |

---

## 常用命令

```bash
# 查看 Railway 日志
railway logs

# 本地测试 Docker 构建
docker build -f Dockerfile.backend -t jxc-backend .
docker build -f Dockerfile.frontend -t jxc-frontend .

# 本地运行
docker compose up -d
```

---

## 安全检查清单

- [x] 密码 BCrypt 加密存储
- [x] JWT 认证 + Token 黑名单
- [x] CORS 跨域限制
- [x] API 全局限流（120次/分钟）
- [x] 登录限流（防暴力破解）
- [x] 敏感日志脱敏
- [x] SQL 注入防护
- [x] XSS 防护（安全头）
- [x] HTTPS 自动配置
- [x] 数据库不暴露端口
- [x] 前端错误监控
- [x] 日志轮转
- [x] 自动备份

---

## 故障排查

### Railway 部署失败
1. 检查构建日志
2. 确认环境变量正确
3. 确认 Dockerfile 路径正确

### 数据库连接失败
1. 检查 `DB_URL` 格式
2. 确认 MySQL 服务已启动
3. 检查密码是否正确

### 前端白屏
1. 检查 `BACKEND_HOST` 环境变量
2. 确认后端服务正常
3. 检查浏览器控制台错误
