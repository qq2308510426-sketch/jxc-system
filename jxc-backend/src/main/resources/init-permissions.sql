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

-- 角色权限初始化：角色名与权限串均取自代码实际使用的集合
-- 角色：super_admin / warehouse_admin / purchaser / salesperson / finance / viewer
-- 说明：PermissionInterceptor 按 (role, permission) 查库判定，无超级管理员硬编码旁路

-- super_admin：超级管理员：全部功能权限
INSERT IGNORE INTO `t_role_permission` (`role`, `permission`) VALUES
('super_admin', 'account:payable'),
('super_admin', 'account:receivable'),
('super_admin', 'account:view'),
('super_admin', 'category:create'),
('super_admin', 'category:delete'),
('super_admin', 'category:edit'),
('super_admin', 'category:view'),
('super_admin', 'customer:create'),
('super_admin', 'customer:delete'),
('super_admin', 'customer:edit'),
('super_admin', 'customer:view'),
('super_admin', 'data:export'),
('super_admin', 'data:import'),
('super_admin', 'log:view'),
('super_admin', 'notification:view'),
('super_admin', 'order:create'),
('super_admin', 'order:delete'),
('super_admin', 'order:edit'),
('super_admin', 'order:ship'),
('super_admin', 'order:view'),
('super_admin', 'product:create'),
('super_admin', 'product:delete'),
('super_admin', 'product:edit'),
('super_admin', 'product:view'),
('super_admin', 'purchase:approve'),
('super_admin', 'purchase:create'),
('super_admin', 'purchase:receive'),
('super_admin', 'purchase:view'),
('super_admin', 'report:view'),
('super_admin', 'stock:in'),
('super_admin', 'stock:manage'),
('super_admin', 'stock:out'),
('super_admin', 'stock:view'),
('super_admin', 'supplier:create'),
('super_admin', 'supplier:delete'),
('super_admin', 'supplier:edit'),
('super_admin', 'supplier:view'),
('super_admin', 'user:create'),
('super_admin', 'user:delete'),
('super_admin', 'user:edit'),
('super_admin', 'user:view'),
('super_admin', 'warehouse:create'),
('super_admin', 'warehouse:delete'),
('super_admin', 'warehouse:edit'),
('super_admin', 'warehouse:view');

-- warehouse_admin：仓库管理员：商品/库存/盘点/批次/仓库维护，采购收货查看
INSERT IGNORE INTO `t_role_permission` (`role`, `permission`) VALUES
('warehouse_admin', 'product:view'),
('warehouse_admin', 'product:edit'),
('warehouse_admin', 'category:view'),
('warehouse_admin', 'supplier:view'),
('warehouse_admin', 'customer:view'),
('warehouse_admin', 'warehouse:view'),
('warehouse_admin', 'warehouse:edit'),
('warehouse_admin', 'stock:view'),
('warehouse_admin', 'stock:in'),
('warehouse_admin', 'stock:out'),
('warehouse_admin', 'stock:manage'),
('warehouse_admin', 'purchase:view'),
('warehouse_admin', 'purchase:receive'),
('warehouse_admin', 'report:view'),
('warehouse_admin', 'notification:view'),
('warehouse_admin', 'log:view');

-- purchaser：采购员：商品与供应商维护、采购单创建审核收货、库存查看
INSERT IGNORE INTO `t_role_permission` (`role`, `permission`) VALUES
('purchaser', 'product:view'),
('purchaser', 'product:create'),
('purchaser', 'product:edit'),
('purchaser', 'category:view'),
('purchaser', 'supplier:view'),
('purchaser', 'supplier:create'),
('purchaser', 'supplier:edit'),
('purchaser', 'purchase:view'),
('purchaser', 'purchase:create'),
('purchaser', 'purchase:approve'),
('purchaser', 'purchase:receive'),
('purchaser', 'stock:view'),
('purchaser', 'stock:in'),
('purchaser', 'warehouse:view'),
('purchaser', 'report:view'),
('purchaser', 'notification:view');

-- salesperson：销售员：客户与订单维护、发货、应收查看
INSERT IGNORE INTO `t_role_permission` (`role`, `permission`) VALUES
('salesperson', 'product:view'),
('salesperson', 'customer:view'),
('salesperson', 'customer:create'),
('salesperson', 'customer:edit'),
('salesperson', 'order:view'),
('salesperson', 'order:create'),
('salesperson', 'order:edit'),
('salesperson', 'order:ship'),
('salesperson', 'stock:view'),
('salesperson', 'report:view'),
('salesperson', 'notification:view'),
('salesperson', 'account:view'),
('salesperson', 'account:receivable');

-- finance：财务：应收应付与收付款、报表与日志查看
INSERT IGNORE INTO `t_role_permission` (`role`, `permission`) VALUES
('finance', 'account:view'),
('finance', 'account:receivable'),
('finance', 'account:payable'),
('finance', 'order:view'),
('finance', 'purchase:view'),
('finance', 'customer:view'),
('finance', 'supplier:view'),
('finance', 'product:view'),
('finance', 'report:view'),
('finance', 'log:view'),
('finance', 'notification:view');

-- viewer：查看者：只读
INSERT IGNORE INTO `t_role_permission` (`role`, `permission`) VALUES
('viewer', 'account:view'),
('viewer', 'category:view'),
('viewer', 'customer:view'),
('viewer', 'log:view'),
('viewer', 'notification:view'),
('viewer', 'order:view'),
('viewer', 'product:view'),
('viewer', 'purchase:view'),
('viewer', 'report:view'),
('viewer', 'stock:view'),
('viewer', 'supplier:view'),
('viewer', 'user:view'),
('viewer', 'warehouse:view');
