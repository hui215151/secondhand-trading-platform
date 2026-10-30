CREATE DATABASE IF NOT EXISTS secondhand DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE secondhand;

CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(200) COMMENT '头像URL',
    phone VARCHAR(20) COMMENT '手机号',
    role TINYINT NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户 1-管理员',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username(username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    user_id BIGINT NOT NULL COMMENT '发布用户ID',
    title VARCHAR(100) NOT NULL COMMENT '商品标题',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    original_price DECIMAL(10,2) COMMENT '原价',
    category_id INT COMMENT '分类ID',
    images VARCHAR(500) COMMENT '图片URL，逗号分隔',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待审核 1-上架 2-下架 3-已售出',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    favorite_count INT DEFAULT 0 COMMENT '收藏次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id(user_id),
    INDEX idx_category_id(category_id),
    INDEX idx_status(status),
    INDEX idx_price(price),
    INDEX idx_create_time(create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE `order` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
    buyer_id BIGINT NOT NULL COMMENT '买家ID',
    seller_id BIGINT NOT NULL COMMENT '卖家ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    goods_title VARCHAR(100) NOT NULL COMMENT '商品标题',
    price DECIMAL(10,2) NOT NULL COMMENT '成交价格',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待付款 1-已付款 2-已发货 3-已完成 4-已取消',
    address VARCHAR(200) COMMENT '收货地址',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    pay_time DATETIME COMMENT '付款时间',
    complete_time DATETIME COMMENT '完成时间',
    INDEX idx_buyer_id(buyer_id),
    INDEX idx_seller_id(seller_id),
    INDEX idx_order_no(order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_goods(user_id, goods_id),
    INDEX idx_user_id(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

INSERT INTO user (username, password, nickname, role) VALUES
('admin', 'admin123', '管理员', 1),
('user1', 'user123', '张三', 0);

INSERT INTO goods (user_id, title, description, price, original_price, category_id, status) VALUES
(2, 'iPhone 13', '九成新，无划痕', 3500.00, 5999.00, 1, 1),
(2, 'MacBook Air', 'M1芯片，电池健康95%', 4500.00, 7999.00, 2, 1);

-- ============================================================
-- 秒杀模块（限时抢购）
-- ============================================================

-- 秒杀商品表
CREATE TABLE seckill_goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    goods_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    category VARCHAR(50) COMMENT '商品分类',
    goods_img VARCHAR(500) COMMENT '商品图片URL',
    original_price DECIMAL(10,2) NOT NULL COMMENT '原价',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    stock_count INT NOT NULL DEFAULT 0 COMMENT '库存',
    start_time DATETIME NOT NULL COMMENT '活动开始时间',
    end_time DATETIME NOT NULL COMMENT '活动结束时间',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_start_end(start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- 秒杀订单表
CREATE TABLE seckill_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    goods_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-创建中 1-已支付 2-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    pay_time DATETIME COMMENT '支付时间',
    UNIQUE KEY uk_user_goods(user_id, goods_id),
    INDEX idx_goods_id(goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

-- 本地事务表（秒杀下单最终一致性）
CREATE TABLE transaction_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    transaction_id VARCHAR(64) NOT NULL COMMENT '事务ID',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    business_id VARCHAR(100) NOT NULL COMMENT '业务ID（如 userId:goodsId）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-处理中 1-成功 2-失败 3-已回补',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_transaction_id(transaction_id),
    INDEX idx_status_create(status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地事务表';

-- 秒杀演示数据（活动时间以数据库当前时间为基准，始终处于进行中）
INSERT INTO seckill_goods (goods_name, category, goods_img, original_price, seckill_price, stock_count, start_time, end_time, version) VALUES
('iPhone 13 秒杀', '手机数码', '', 5999.00, 3999.00, 10, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 2 DAY), 0),
('MacBook Air M1 秒杀', '电脑办公', '', 7999.00, 4999.00, 5, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 2 DAY), 0),
('AirPods Pro 2 秒杀', '智能穿戴', '', 1899.00, 1299.00, 20, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 2 DAY), 0);