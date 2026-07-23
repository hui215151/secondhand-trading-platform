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