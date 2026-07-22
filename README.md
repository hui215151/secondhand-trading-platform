# 校园二手交易平台

基于 SpringBoot 的 C2C 二手交易后端系统，支持用户发布商品、搜索筛选、下单交易、评论收藏、后台审核等完整业务流程。

## 技术栈
- SpringBoot 2.7.x
- MyBatis-Plus
- Redis（缓存、Token存储、防重提交）
- MySQL 8.x
- JWT（无状态登录）
- 阿里云 OSS（图片存储）
- PageHelper（分页）

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
3. 修改 `application.yml` 中的 MySQL、Redis、OSS 配置
4. 启动项目

## 接口文档
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/user/login | POST | 登录 |
| /api/user/register | POST | 注册 |
| /api/goods/list | GET | 商品列表（支持分页、搜索、价格区间） |
| /api/goods/detail/{id} | GET | 商品详情 |
| /api/order/create | POST | 创建订单 |
| /api/favorite/add/{goodsId} | POST | 收藏商品 |
