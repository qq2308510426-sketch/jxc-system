-- H2 compatible schema for testing
DROP TABLE IF EXISTS t_account_payable;
DROP TABLE IF EXISTS t_account_receivable;
DROP TABLE IF EXISTS t_purchase_order_item;
DROP TABLE IF EXISTS t_purchase_order;
DROP TABLE IF EXISTS t_inventory_check_item;
DROP TABLE IF EXISTS t_inventory_check;
DROP TABLE IF EXISTS t_product_stock;
DROP TABLE IF EXISTS t_product_batch;
DROP TABLE IF EXISTS t_return_item;
DROP TABLE IF EXISTS t_return;
DROP TABLE IF EXISTS t_payment;
DROP TABLE IF EXISTS t_notification;
DROP TABLE IF EXISTS t_role_permission;
DROP TABLE IF EXISTS t_log;
DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_stock_out;
DROP TABLE IF EXISTS t_stock_in;
DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_product;
DROP TABLE IF EXISTS t_customer;
DROP TABLE IF EXISTS t_supplier;
DROP TABLE IF EXISTS t_category;
DROP TABLE IF EXISTS t_warehouse;
DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'viewer',
    status TINYINT NOT NULL DEFAULT 1,
    warehouse_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_supplier (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
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
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(50),
    phone VARCHAR(20),
    address VARCHAR(255),
    credit_level VARCHAR(20) DEFAULT 'normal',
    credit_limit DECIMAL(12,2) DEFAULT NULL,
    current_balance DECIMAL(12,2) DEFAULT 0.00,
    remark VARCHAR(500),
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_warehouse (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    contact VARCHAR(50),
    phone VARCHAR(20),
    status INT DEFAULT 1,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
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
    version INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_product_batch (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    batch_no VARCHAR(50) NOT NULL,
    serial_no VARCHAR(100),
    quantity INT DEFAULT 0,
    production_date DATE,
    expiry_date DATE,
    supplier_id BIGINT,
    purchase_price DECIMAL(10,2),
    status INT DEFAULT 1,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_product_stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    quantity INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_stock_in (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    supplier_id BIGINT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    operator_id BIGINT NOT NULL,
    warehouse_id BIGINT,
    batch_no VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_stock_out (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    customer_id BIGINT,
    order_id BIGINT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    operator_id BIGINT NOT NULL,
    warehouse_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    status TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    operator_id BIGINT NOT NULL,
    shipping_no VARCHAR(100),
    shipping_company VARCHAR(100),
    shipping_status INT DEFAULT 0,
    shipping_time TIMESTAMP,
    payment_status TINYINT NOT NULL DEFAULT 0,
    paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL
);

CREATE TABLE t_payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    payment_no VARCHAR(50) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(20) DEFAULT 'cash',
    remark VARCHAR(500),
    operator_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_return (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    return_no VARCHAR(50) NOT NULL,
    order_id BIGINT,
    customer_id BIGINT,
    type INT DEFAULT 1,
    status INT DEFAULT 0,
    reason VARCHAR(500),
    total_amount DECIMAL(12,2) DEFAULT 0.00,
    operator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_return_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    return_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2),
    subtotal DECIMAL(10,2),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_inventory_check (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    check_no VARCHAR(50) NOT NULL,
    status INT DEFAULT 0,
    remark VARCHAR(500),
    operator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_inventory_check_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    check_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    system_stock INT,
    actual_stock INT,
    difference INT,
    remark VARCHAR(200),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(50),
    title VARCHAR(200),
    content TEXT,
    related_id BIGINT,
    is_read TINYINT DEFAULT 0,
    user_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role VARCHAR(50) NOT NULL,
    permission VARCHAR(100) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    detail TEXT,
    ip VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_purchase_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    payment_status TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    operator_id BIGINT NOT NULL,
    approver_id BIGINT,
    approve_time TIMESTAMP,
    receive_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE t_purchase_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    purchase_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    received_quantity INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_account_receivable (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    order_id BIGINT,
    amount DECIMAL(12,2) NOT NULL,
    paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    due_date DATE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_account_payable (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL,
    purchase_order_id BIGINT,
    amount DECIMAL(12,2) NOT NULL,
    paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    due_date DATE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert admin user (password: admin123, BCrypt)
INSERT INTO t_user (username, password, real_name, role, status)
VALUES ('admin', '$2a$10$HprFdIlZqldaZZ05mxKoEeaf8Llwe8kNtcgRp8WBDD6Rcv4NqaLRS', 'Admin', 'super_admin', 1);

-- Insert basic permissions for testing
INSERT INTO t_role_permission (role, permission) VALUES
('super_admin', 'product:view'), ('super_admin', 'product:create'),
('super_admin', 'order:view'), ('super_admin', 'order:create'),
('super_admin', 'user:view'), ('super_admin', 'user:create'),
('super_admin', 'stock:view'), ('super_admin', 'stock:in'),
('super_admin', 'report:view'),
('salesperson', 'product:view'),
('salesperson', 'order:view'), ('salesperson', 'order:create'),
('salesperson', 'customer:view'), ('salesperson', 'customer:create'),
('viewer', 'product:view'), ('viewer', 'order:view');