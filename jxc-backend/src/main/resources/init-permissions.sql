-- ============================================================
-- 进销存管理系统 - 权限初始化脚本
-- 请在 jxc_db 数据库中执行此脚本
-- ============================================================

-- 确保角色权限表存在
CREATE TABLE IF NOT EXISTS `t_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role` VARCHAR(50) NOT NULL,
    `permission` VARCHAR(100) NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role`, `permission`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 清空已有权限数据（如需保留请注释此行）
-- TRUNCATE TABLE `t_role_permission`;

-- ============================================================
-- 管理员 (admin) - 拥有所有权限（代码中已硬编码跳过检查，此记录仅供参考）
-- ============================================================
INSERT IGNORE INTO `t_role_permission` (`role`, `permission`) VALUES ('admin', 'all');

-- ============================================================
-- 仓库管理员 (warehouse_manager)
-- 职责：商品查看、库存管理（入库/出库/盘点）、仓库管理、批次管理
-- ============================================================
INSERT IGNORE INTO `t_role_permission` (`role`, `permission`) VALUES
('warehouse_manager', 'product:view'),
('warehouse_manager', 'stock:view'),
('warehouse_manager', 'stock:in'),
('warehouse_manager', 'stock:out'),
('warehouse_manager', 'stock:manage'),
('warehouse_manager', 'notification:view');

-- ============================================================
-- 销售员 (sales)
-- 职责：订单创建和管理、客户管理、商品查看、库存查看
-- ============================================================
INSERT IGNORE INTO `t_role_permission` (`role`, `permission`) VALUES
('sales', 'product:view'),
('sales', 'order:view'),
('sales', 'order:create'),
('sales', 'order:manage'),
('sales', 'stock:view'),
('sales', 'customer:view' ),
('sales', 'notification:view');

-- ============================================================
-- 采购员 (purchaser)
-- 职责：商品管理、供应商管理、入库操作、库存查看、报表查看
-- ============================================================
INSERT IGNORE INTO `t_role_permission` (`role`, `permission`) VALUES
('purchaser', 'product:view'),
('purchaser', 'product:manage'),
('purchaser', 'stock:view'),
('purchaser', 'stock:in'),
('purchaser', 'report:view'),
('purchaser', 'notification:view');

-- ============================================================
-- 财务 (finance)
-- 职责：订单查看、付款管理、报表查看、客户/供应商对账
-- ============================================================
INSERT IGNORE INTO `t_role_permission` (`role`, `permission`) VALUES
('finance', 'order:view'),
('finance', 'order:manage'),
('finance', 'report:view'),
('finance', 'notification:view');

-- ============================================================
-- 查看员 (viewer) - 只读权限
-- 职责：查看所有数据，不做修改
-- ============================================================
INSERT IGNORE INTO `t_role_permission` (`role`, `permission`) VALUES
('viewer', 'product:view'),
('viewer', 'order:view'),
('viewer', 'stock:view'),
('viewer', 'report:view'),
('viewer', 'notification:view');