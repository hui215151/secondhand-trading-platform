# 校园二手交易平台

基于 SpringBoot 的 C2C 二手交易后端系统，支持用户发布商品、搜索筛选、下单交易、评论收藏、后台审核等完整业务流程。

## 技术栈
- SpringBoot 2.7.x
- MyBatis-Plus
- Redis（缓存、Token存储、防重提交）
- MySQL 8.x
- JWT（无状态登录）
- 阿里云 OSS（图片存储，未配置时可用本地 `/api/file/upload` 兜底）
- PageHelper（分页）
- **前端：Vue 3 + Vue Router + Vite**（`frontend/` 目录，零 UI 框架依赖）

## 核心功能
| 模块 | 功能 |
|------|------|
| 用户模块 | 注册、登录（Token+Redis）、个人信息 |
| 商品模块 | 发布、搜索（多条件+分页）、详情、上下架 |
| 订单模块 | 创建订单（事务+防重）、付款、取消 |
| 收藏模块 | 收藏/取消收藏、收藏列表 |
| 后台管理 | 用户管理、商品审核 |

## 技术亮点
- **统一异常处理**：@RestControllerAdvice 全局捕获
- **无状态登录**：JWT + Redis，支持Token自动续期
- **缓存优化**：Redis缓存热门商品、商品列表，延迟双删保证一致性
- **接口幂等**：Redis SETNX防重提交
- **事务保障**：@Transactional保证订单创建与商品状态更新原子性
- **参数校验**：JSR-303注解自动校验
- **云端存储**：阿里云OSS封装通用上传工具

## 快速启动
1. 克隆项目
2. 创建数据库，执行 `sql/secondhand.sql`
3. 复制 `src/main/resources/application-local.yml.example` 为 `application-local.yml`，填入自己的 MySQL / Redis / OSS 配置（该文件已被 `.gitignore` 忽略，不会提交）
4. 启动项目：`mvn spring-boot:run`
5. 浏览器打开 <http://localhost:8081>（前端构建产物已放在 `src/main/resources/static`，无需额外启动前端）

演示账号：`user1 / user123`（普通用户）、`admin / admin123`（管理员）

### 前端（Vue 3 + Vite）
页面代码在 `frontend/`，构建产物输出到 `src/main/resources/static`，所以打包后由 8081 一个端口同时提供页面和接口。

```bash
cd frontend
npm install          # 首次
npm run dev          # 开发模式：http://localhost:5173，/api 与 /uploads 自动代理到 8081
npm run build        # 改完页面后重新构建到 src/main/resources/static
```

> 开发时后端要同时跑在 8081，否则 `npm run dev` 的代理会 502。
> 只改后端时不需要动前端；只改页面时用 `npm run dev` 最方便，不必重启 Java。

页面结构：

| 路由 | 页面 |
|------|------|
| `#/` | 首页：搜索、分类、价格区间、分页 |
| `#/goods/:id` | 商品详情：收藏、立即购买 |
| `#/publish` | 发布闲置（含图片上传预览） |
| `#/me` | 个人中心：我的订单 / 收藏 / 我发布的 |
| `#/admin` | 管理后台：商品审核、用户管理（仅管理员可见） |

### 配置方式
`application.yml` 里只保留默认值与环境变量占位符，敏感信息有三种写法（优先级从低到高）：

| 方式 | 示例 |
|------|------|
| 默认值 | `${DB_PORT:3306}` |
| 环境变量 | `DB_PASSWORD=xxx`，或 `SPRING_DATASOURCE_PASSWORD=xxx` |
| 本地私有文件 | `application-local.yml`（推荐，不入库） |

可覆盖的环境变量：`SERVER_PORT`、`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`、`REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`、`REDIS_DATABASE`、`OSS_ENDPOINT`、`OSS_ACCESS_KEY_ID`、`OSS_ACCESS_KEY_SECRET`、`OSS_BUCKET_NAME`。

### 接口鉴权约定
- 除 `/api/user/login`、`/api/user/register`、`/api/goods/list`、`/api/goods/detail/**` 外，其余接口都需要请求头 `Authorization: Bearer <token>`
- 未登录 / token 过期返回 **HTTP 401** + `{"code":401,...}`
- 普通用户访问 `/api/admin/**` 返回 **HTTP 403**
- 注册接口只接受 `username / password / nickname / phone`，角色固定为普通用户

## 接口文档
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/user/login | POST | 登录 |
| /api/user/register | POST | 注册 |
| /api/user/info | GET | 当前登录用户信息 |
| /api/user/profile/{id} | GET | 卖家公开资料（昵称、头像） |
| /api/goods/list | GET | 商品列表（支持分页、搜索、价格区间） |
| /api/goods/detail/{id} | GET | 商品详情 |
| /api/goods/mine | GET | 我发布的商品 |
| /api/goods/publish | POST | 发布商品（待审核） |
| /api/order/create | POST | 创建订单 |
| /api/order/list | GET | 我的订单（role=buyer/seller/all） |
| /api/order/pay/{orderNo} | POST | 付款 |
| /api/order/cancel/{orderNo} | POST | 取消订单 |
| /api/favorite/add/{goodsId} | POST | 收藏商品 |
| /api/favorite/check/{goodsId} | GET | 是否已收藏 |
| /api/file/upload | POST | 本地图片上传（OSS 兜底） |
| /api/admin/users | GET | 用户列表（管理员） |
| /api/admin/goods/pending | GET | 待审核商品（管理员） |
| /api/admin/goods/audit/{id} | POST | 审核通过（管理员） |
