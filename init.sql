-- JXC Management System - Database Initialization Script
-- Run this script on a fresh MySQL instance

CREATE DATABASE IF NOT EXISTS jxc_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE jxc_db;

-- ==================== Core Tables ====================

CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'viewer',
    status TINYINT NOT NULL DEFAULT 1,
    warehouse_id BIGINT COMMENT '关联仓库ID(数据隔离)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS t_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort INT NOT NULL DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类';

CREATE TABLE IF NOT EXISTS t_supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(50),
    phone VARCHAR(20),
    address VARCHAR(255),
    remark VARCHAR(500),
    status TINYINT NOT NULL DEFAULT 1,
    delivery_days INT DEFAULT 7,
    quality_score DECIMAL(3,1) DEFAULT 5.0,
    total_orders INT DEFAULT 0,
    on_time_orders INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商';

CREATE TABLE IF NOT EXISTS t_customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(50),
    phone VARCHAR(20),
    address VARCHAR(255),
    credit_level VARCHAR(20) DEFAULT 'normal',
    credit_limit DECIMAL(12,2) DEFAULT NULL,
    current_balance DECIMAL(12,2) DEFAULT 0.00,
    remark VARCHAR(500),
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户';

CREATE TABLE IF NOT EXISTS t_warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    contact VARCHAR(50),
    phone VARCHAR(20),
    status INT DEFAULT 1,
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库';

CREATE TABLE IF NOT EXISTS t_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    category_id BIGINT,
    spec VARCHAR(200),
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    stock INT NOT NULL DEFAULT 0,
    reserved_stock INT NOT NULL DEFAULT 0,
    safety_stock INT NOT NULL DEFAULT 0,
    supplier_id BIGINT,
    status TINYINT NOT NULL DEFAULT 1,
    image_url VARCHAR(500) DEFAULT NULL,
    cost_price DECIMAL(10,2) DEFAULT NULL,
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category_id),
    INDEX idx_supplier (supplier_id),
    INDEX idx_status (status),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品';

-- ==================== Business Tables ====================

CREATE TABLE IF NOT EXISTS t_stock_in (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    supplier_id BIGINT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    operator_id BIGINT NOT NULL,
    warehouse_id BIGINT,
    batch_no VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product (product_id),
    INDEX idx_supplier (supplier_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库记录';

CREATE TABLE IF NOT EXISTS t_stock_out (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    customer_id BIGINT,
    order_id BIGINT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    operator_id BIGINT NOT NULL,
    warehouse_id BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product (product_id),
    INDEX idx_order (order_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库记录';

CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    status TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    operator_id BIGINT NOT NULL,
    shipping_no VARCHAR(100),
    shipping_company VARCHAR(100),
    shipping_status INT DEFAULT 0,
    shipping_time DATETIME,
    payment_status TINYINT NOT NULL DEFAULT 0,
    paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer (customer_id),
    INDEX idx_status (status),
    INDEX idx_order_no (order_no),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单';

CREATE TABLE IF NOT EXISTS t_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    INDEX idx_order (order_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

CREATE TABLE IF NOT EXISTS t_purchase_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    payment_status TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    operator_id BIGINT NOT NULL,
    approver_id BIGINT,
    approve_time DATETIME,
    receive_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_supplier (supplier_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单';

CREATE TABLE IF NOT EXISTS t_purchase_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    received_quantity INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order (purchase_order_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细';

CREATE TABLE IF NOT EXISTS t_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_no VARCHAR(50) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(20) DEFAULT 'cash',
    remark VARCHAR(500),
    operator_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='付款记录';

CREATE TABLE IF NOT EXISTS t_return (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_no VARCHAR(50) NOT NULL,
    order_id BIGINT,
    customer_id BIGINT,
    type INT DEFAULT 1,
    status INT DEFAULT 0,
    reason VARCHAR(500),
    total_amount DECIMAL(12,2) DEFAULT 0.00,
    operator_id BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退货单';

CREATE TABLE IF NOT EXISTS t_return_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2),
    subtotal DECIMAL(10,2),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_return (return_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退货明细';

CREATE TABLE IF NOT EXISTS t_account_receivable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    order_id BIGINT,
    amount DECIMAL(12,2) NOT NULL,
    paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    due_date DATE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer (customer_id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应收账款';

CREATE TABLE IF NOT EXISTS t_account_payable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    purchase_order_id BIGINT,
    amount DECIMAL(12,2) NOT NULL,
    paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    due_date DATE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_supplier (supplier_id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应付账款';

-- ==================== System Tables ====================

CREATE TABLE IF NOT EXISTS t_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role VARCHAR(50) NOT NULL,
    permission VARCHAR(100) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限';

CREATE TABLE IF NOT EXISTS t_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50),
    title VARCHAR(200),
    content TEXT,
    related_id BIGINT,
    is_read TINYINT DEFAULT 0,
    user_id BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知';

CREATE TABLE IF NOT EXISTS t_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    detail TEXT,
    ip VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_action (action),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- ==================== Seed Data ====================

-- Default admin (password: admin123)
INSERT INTO t_user (username, password, real_name, role, status)
VALUES ('admin', '$2a$10$I2zvXtlVufguywazyL.RkedBthuqpkIZ9gQC1PA7ac2tJbDqYtb2O', 'Admin', 'super_admin', 1)
ON DUPLICATE KEY UPDATE role = 'super_admin';

-- Default warehouse
INSERT INTO t_warehouse (name, address, contact, status)
VALUES ('默认仓库', '总部仓库', '管理员', 1)
ON DUPLICATE KEY UPDATE name = name;

-- Role permissions
INSERT IGNORE INTO t_role_permission (role, permission) VALUES
('super_admin', 'product:view'), ('super_admin', 'product:create'), ('super_admin', 'product:edit'), ('super_admin', 'product:delete'),
('super_admin', 'category:view'), ('super_admin', 'category:create'), ('super_admin', 'category:edit'), ('super_admin', 'category:delete'),
('super_admin', 'supplier:view'), ('super_admin', 'supplier:create'), ('super_admin', 'supplier:edit'), ('super_admin', 'supplier:delete'),
('super_admin', 'customer:view'), ('super_admin', 'customer:create'), ('super_admin', 'customer:edit'), ('super_admin', 'customer:delete'),
('super_admin', 'order:view'), ('super_admin', 'order:create'), ('super_admin', 'order:edit'), ('super_admin', 'order:delete'), ('super_admin', 'order:ship'),
('super_admin', 'stock:view'), ('super_admin', 'stock:in'), ('super_admin', 'stock:out'), ('super_admin', 'stock:manage'),
('super_admin', 'warehouse:view'), ('super_admin', 'warehouse:create'), ('super_admin', 'warehouse:edit'), ('super_admin', 'warehouse:delete'),
('super_admin', 'purchase:view'), ('super_admin', 'purchase:create'), ('super_admin', 'purchase:approve'), ('super_admin', 'purchase:receive'),
('super_admin', 'account:view'), ('super_admin', 'account:receivable'), ('super_admin', 'account:payable'),
('super_admin', 'report:view'), ('super_admin', 'report:export'),
('super_admin', 'user:view'), ('super_admin', 'user:create'), ('super_admin', 'user:edit'), ('super_admin', 'user:delete'),
('super_admin', 'log:view'), ('super_admin', 'notification:view'), ('super_admin', 'data:import'), ('super_admin', 'data:export'),
('warehouse_admin', 'product:view'), ('warehouse_admin', 'stock:view'), ('warehouse_admin', 'stock:in'), ('warehouse_admin', 'stock:out'), ('warehouse_admin', 'stock:manage'),
('warehouse_admin', 'warehouse:view'), ('warehouse_admin', 'warehouse:edit'), ('warehouse_admin', 'purchase:view'), ('warehouse_admin', 'purchase:receive'),
('warehouse_admin', 'report:view'), ('warehouse_admin', 'notification:view'),
('purchaser', 'product:view'), ('purchaser', 'product:create'), ('purchaser', 'product:edit'),
('purchaser', 'supplier:view'), ('purchaser', 'supplier:create'), ('purchaser', 'supplier:edit'),
('purchaser', 'purchase:view'), ('purchaser', 'purchase:create'), ('purchaser', 'account:view'), ('purchaser', 'account:payable'),
('purchaser', 'report:view'), ('purchaser', 'notification:view'),
('salesperson', 'product:view'), ('salesperson', 'customer:view'), ('salesperson', 'customer:create'), ('salesperson', 'customer:edit'),
('salesperson', 'order:view'), ('salesperson', 'order:create'), ('salesperson', 'order:edit'),
('salesperson', 'account:view'), ('salesperson', 'account:receivable'), ('salesperson', 'report:view'), ('salesperson', 'notification:view'),
('finance', 'product:view'), ('finance', 'customer:view'), ('finance', 'supplier:view'), ('finance', 'order:view'), ('finance', 'purchase:view'),
('finance', 'account:view'), ('finance', 'account:receivable'), ('finance', 'account:payable'),
('finance', 'report:view'), ('finance', 'report:export'), ('finance', 'notification:view'),
('viewer', 'product:view'), ('viewer', 'category:view'), ('viewer', 'supplier:view'), ('viewer', 'customer:view'),
('viewer', 'order:view'), ('viewer', 'stock:view'), ('viewer', 'warehouse:view'), ('viewer', 'purchase:view'),
('viewer', 'account:view'), ('viewer', 'report:view'), ('viewer', 'notification:view');