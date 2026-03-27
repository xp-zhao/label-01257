-- =============================================
-- 物流仓库管理系统 数据库脚本
-- Database: MySQL 8.0
-- Charset: utf8mb4
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `wms_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `wms_db`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- 角色表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ----------------------------
-- 权限表
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父级ID',
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    `component` VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
    `type` TINYINT DEFAULT 1 COMMENT '类型：1-菜单，2-按钮',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ----------------------------
-- 用户角色关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ----------------------------
-- 角色权限关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (`id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ----------------------------
-- 库房表
-- ----------------------------
DROP TABLE IF EXISTS `wms_warehouse`;
CREATE TABLE `wms_warehouse` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库房ID',
    `warehouse_code` VARCHAR(50) NOT NULL COMMENT '库房编码',
    `warehouse_name` VARCHAR(100) NOT NULL COMMENT '库房名称',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `total_area` DECIMAL(10,2) DEFAULT 0 COMMENT '总面积(平方米)',
    `used_area` DECIMAL(10,2) DEFAULT 0 COMMENT '已用面积(平方米)',
    `manager_id` BIGINT DEFAULT NULL COMMENT '负责人ID',
    `manager_name` VARCHAR(50) DEFAULT NULL COMMENT '负责人姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_warehouse_code` (`warehouse_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库房表';

-- ----------------------------
-- 库房区域表
-- ----------------------------
DROP TABLE IF EXISTS `wms_warehouse_area`;
CREATE TABLE `wms_warehouse_area` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '区域ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '库房ID',
    `area_code` VARCHAR(50) NOT NULL COMMENT '区域编码',
    `area_name` VARCHAR(100) NOT NULL COMMENT '区域名称',
    `area_type` VARCHAR(20) DEFAULT 'STORAGE' COMMENT '区域类型：STORAGE-存储区，PICKING-拣货区，RECEIVING-收货区，SHIPPING-发货区，TEMPORARY-暂存区',
    `area_size` DECIMAL(10,2) DEFAULT 0 COMMENT '区域面积(平方米)',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库房区域表';

-- ----------------------------
-- 货架表
-- ----------------------------
DROP TABLE IF EXISTS `wms_shelf`;
CREATE TABLE `wms_shelf` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '货架ID',
    `area_id` BIGINT NOT NULL COMMENT '区域ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '库房ID',
    `shelf_code` VARCHAR(50) NOT NULL COMMENT '货架编码',
    `shelf_name` VARCHAR(100) NOT NULL COMMENT '货架名称',
    `layer_count` INT DEFAULT 1 COMMENT '层数',
    `column_count` INT DEFAULT 1 COMMENT '列数',
    `max_weight` DECIMAL(10,2) DEFAULT 0 COMMENT '最大承重(kg)',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_area_id` (`area_id`),
    KEY `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='货架表';

-- ----------------------------
-- 商品表
-- ----------------------------
DROP TABLE IF EXISTS `wms_product`;
CREATE TABLE `wms_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `product_code` VARCHAR(50) NOT NULL COMMENT '商品编码',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '商品分类',
    `brand` VARCHAR(50) DEFAULT NULL COMMENT '品牌',
    `unit` VARCHAR(20) DEFAULT '件' COMMENT '计量单位',
    `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
    `weight` DECIMAL(10,3) DEFAULT 0 COMMENT '重量(kg)',
    `volume` DECIMAL(10,3) DEFAULT 0 COMMENT '体积(立方米)',
    `price` DECIMAL(10,2) DEFAULT 0 COMMENT '单价',
    `min_stock` INT DEFAULT 0 COMMENT '最低库存',
    `max_stock` INT DEFAULT 0 COMMENT '最高库存',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_code` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ----------------------------
-- 库存表
-- ----------------------------
DROP TABLE IF EXISTS `wms_inventory`;
CREATE TABLE `wms_inventory` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_code` VARCHAR(50) DEFAULT NULL COMMENT '商品编码',
    `product_name` VARCHAR(100) DEFAULT NULL COMMENT '商品名称',
    `warehouse_id` BIGINT NOT NULL COMMENT '库房ID',
    `warehouse_name` VARCHAR(100) DEFAULT NULL COMMENT '库房名称',
    `area_id` BIGINT DEFAULT NULL COMMENT '区域ID',
    `shelf_id` BIGINT DEFAULT NULL COMMENT '货架ID',
    `location_code` VARCHAR(50) DEFAULT NULL COMMENT '库位编码',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `quantity` INT DEFAULT 0 COMMENT '库存数量',
    `available_qty` INT DEFAULT 0 COMMENT '可用数量',
    `locked_qty` INT DEFAULT 0 COMMENT '锁定数量',
    `production_date` DATE DEFAULT NULL COMMENT '生产日期',
    `expiry_date` DATE DEFAULT NULL COMMENT '过期日期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_warehouse_id` (`warehouse_id`),
    KEY `idx_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- ----------------------------
-- 入库单表
-- ----------------------------
DROP TABLE IF EXISTS `wms_inbound_order`;
CREATE TABLE `wms_inbound_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '入库单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '入库单号',
    `warehouse_id` BIGINT NOT NULL COMMENT '库房ID',
    `warehouse_name` VARCHAR(100) DEFAULT NULL COMMENT '库房名称',
    `supplier` VARCHAR(100) DEFAULT NULL COMMENT '供应商',
    `inbound_type` VARCHAR(20) DEFAULT 'PURCHASE' COMMENT '入库类型：PURCHASE-采购入库，RETURN-退货入库，TRANSFER-调拨入库，OTHER-其他',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-草稿，1-待入库，2-入库中，3-已完成，4-已取消',
    `total_quantity` INT DEFAULT 0 COMMENT '总数量',
    `actual_quantity` INT DEFAULT 0 COMMENT '实际入库数量',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作员ID',
    `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作员姓名',
    `plan_time` DATETIME DEFAULT NULL COMMENT '计划入库时间',
    `inbound_time` DATETIME DEFAULT NULL COMMENT '实际入库时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_warehouse_id` (`warehouse_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库单表';

-- ----------------------------
-- 入库单明细表
-- ----------------------------
DROP TABLE IF EXISTS `wms_inbound_order_item`;
CREATE TABLE `wms_inbound_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `order_id` BIGINT NOT NULL COMMENT '入库单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_code` VARCHAR(50) DEFAULT NULL COMMENT '商品编码',
    `product_name` VARCHAR(100) DEFAULT NULL COMMENT '商品名称',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `plan_quantity` INT DEFAULT 0 COMMENT '计划数量',
    `actual_quantity` INT DEFAULT 0 COMMENT '实际数量',
    `shelf_id` BIGINT DEFAULT NULL COMMENT '上架货架ID',
    `location_code` VARCHAR(50) DEFAULT NULL COMMENT '库位编码',
    `production_date` DATE DEFAULT NULL COMMENT '生产日期',
    `expiry_date` DATE DEFAULT NULL COMMENT '过期日期',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库单明细表';

-- ----------------------------
-- 出库单表
-- ----------------------------
DROP TABLE IF EXISTS `wms_outbound_order`;
CREATE TABLE `wms_outbound_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '出库单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '出库单号',
    `warehouse_id` BIGINT NOT NULL COMMENT '库房ID',
    `warehouse_name` VARCHAR(100) DEFAULT NULL COMMENT '库房名称',
    `customer` VARCHAR(100) DEFAULT NULL COMMENT '客户',
    `outbound_type` VARCHAR(20) DEFAULT 'SALES' COMMENT '出库类型：SALES-销售出库，RETURN-退货出库，TRANSFER-调拨出库，OTHER-其他',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-草稿，1-待审核，2-已审核，3-拣货中，4-已出库，5-已取消',
    `total_quantity` INT DEFAULT 0 COMMENT '总数量',
    `actual_quantity` INT DEFAULT 0 COMMENT '实际出库数量',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作员ID',
    `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作员姓名',
    `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `auditor_name` VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `plan_time` DATETIME DEFAULT NULL COMMENT '计划出库时间',
    `outbound_time` DATETIME DEFAULT NULL COMMENT '实际出库时间',
    `delivery_address` VARCHAR(255) DEFAULT NULL COMMENT '送货地址',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_warehouse_id` (`warehouse_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库单表';

-- ----------------------------
-- 出库单明细表
-- ----------------------------
DROP TABLE IF EXISTS `wms_outbound_order_item`;
CREATE TABLE `wms_outbound_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `order_id` BIGINT NOT NULL COMMENT '出库单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_code` VARCHAR(50) DEFAULT NULL COMMENT '商品编码',
    `product_name` VARCHAR(100) DEFAULT NULL COMMENT '商品名称',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `plan_quantity` INT DEFAULT 0 COMMENT '计划数量',
    `actual_quantity` INT DEFAULT 0 COMMENT '实际数量',
    `inventory_id` BIGINT DEFAULT NULL COMMENT '库存ID',
    `shelf_id` BIGINT DEFAULT NULL COMMENT '货架ID',
    `location_code` VARCHAR(50) DEFAULT NULL COMMENT '库位编码',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库单明细表';

-- ----------------------------
-- 存储策略表
-- ----------------------------
DROP TABLE IF EXISTS `wms_storage_strategy`;
CREATE TABLE `wms_storage_strategy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '策略ID',
    `strategy_name` VARCHAR(100) NOT NULL COMMENT '策略名称',
    `strategy_code` VARCHAR(50) NOT NULL COMMENT '策略编码',
    `warehouse_id` BIGINT DEFAULT NULL COMMENT '适用库房ID',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '适用商品分类',
    `rule_type` VARCHAR(20) DEFAULT 'FIFO' COMMENT '规则类型：FIFO-先进先出，LIFO-后进先出，FEFO-先到期先出',
    `rule_content` TEXT DEFAULT NULL COMMENT '规则内容(JSON)',
    `priority` INT DEFAULT 0 COMMENT '优先级',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_strategy_code` (`strategy_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='存储策略表';

-- ----------------------------
-- 拣货策略表
-- ----------------------------
DROP TABLE IF EXISTS `wms_picking_strategy`;
CREATE TABLE `wms_picking_strategy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '策略ID',
    `strategy_name` VARCHAR(100) NOT NULL COMMENT '策略名称',
    `strategy_code` VARCHAR(50) NOT NULL COMMENT '策略编码',
    `warehouse_id` BIGINT DEFAULT NULL COMMENT '适用库房ID',
    `picking_type` VARCHAR(20) DEFAULT 'SINGLE' COMMENT '拣货方式：SINGLE-单品拣货，BATCH-批量拣货，WAVE-波次拣货',
    `rule_content` TEXT DEFAULT NULL COMMENT '规则内容(JSON)',
    `priority` INT DEFAULT 0 COMMENT '优先级',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_strategy_code` (`strategy_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拣货策略表';

-- ----------------------------
-- 补货策略表
-- ----------------------------
DROP TABLE IF EXISTS `wms_replenish_strategy`;
CREATE TABLE `wms_replenish_strategy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '策略ID',
    `strategy_name` VARCHAR(100) NOT NULL COMMENT '策略名称',
    `strategy_code` VARCHAR(50) NOT NULL COMMENT '策略编码',
    `warehouse_id` BIGINT DEFAULT NULL COMMENT '适用库房ID',
    `product_id` BIGINT DEFAULT NULL COMMENT '适用商品ID',
    `min_quantity` INT DEFAULT 0 COMMENT '最小库存',
    `max_quantity` INT DEFAULT 0 COMMENT '最大库存',
    `replenish_quantity` INT DEFAULT 0 COMMENT '补货数量',
    `trigger_type` VARCHAR(20) DEFAULT 'AUTO' COMMENT '触发方式：AUTO-自动，MANUAL-手动',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_strategy_code` (`strategy_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='补货策略表';

-- ----------------------------
-- 司机表
-- ----------------------------
DROP TABLE IF EXISTS `wms_driver`;
CREATE TABLE `wms_driver` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '司机ID',
    `driver_code` VARCHAR(50) DEFAULT NULL COMMENT '司机工号',
    `driver_name` VARCHAR(50) NOT NULL COMMENT '司机姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `id_card` VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
    `license_no` VARCHAR(50) DEFAULT NULL COMMENT '驾驶证号',
    `license_type` VARCHAR(10) DEFAULT NULL COMMENT '驾驶证类型',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-离职，1-空闲，2-任务中',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='司机表';

-- ----------------------------
-- 车辆表
-- ----------------------------
DROP TABLE IF EXISTS `wms_vehicle`;
CREATE TABLE `wms_vehicle` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '车辆ID',
    `plate_no` VARCHAR(20) NOT NULL COMMENT '车牌号',
    `vehicle_type` VARCHAR(50) DEFAULT NULL COMMENT '车辆类型',
    `brand` VARCHAR(50) DEFAULT NULL COMMENT '品牌',
    `model` VARCHAR(50) DEFAULT NULL COMMENT '型号',
    `load_capacity` DECIMAL(10,2) DEFAULT 0 COMMENT '载重(kg)',
    `volume` DECIMAL(10,2) DEFAULT 0 COMMENT '容积(m³)',
    `purchase_date` DATE DEFAULT NULL COMMENT '购买日期',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-报废，1-空闲，2-使用中，3-维修中',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_plate_no` (`plate_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车辆表';

-- ----------------------------
-- 运输任务表
-- ----------------------------
DROP TABLE IF EXISTS `wms_transport_task`;
CREATE TABLE `wms_transport_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `task_no` VARCHAR(50) NOT NULL COMMENT '任务编号',
    `task_type` VARCHAR(20) DEFAULT 'DELIVERY' COMMENT '任务类型：DELIVERY-配送，TRANSFER-调拨，PICKUP-取货',
    `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
    `order_no` VARCHAR(50) DEFAULT NULL COMMENT '关联订单号',
    `order_type` VARCHAR(20) DEFAULT NULL COMMENT '订单类型：INBOUND-入库，OUTBOUND-出库',
    `driver_id` BIGINT DEFAULT NULL COMMENT '司机ID',
    `driver_name` VARCHAR(50) DEFAULT NULL COMMENT '司机姓名',
    `driver_phone` VARCHAR(20) DEFAULT NULL COMMENT '司机电话',
    `vehicle_id` BIGINT DEFAULT NULL COMMENT '车辆ID',
    `plate_no` VARCHAR(20) DEFAULT NULL COMMENT '车牌号',
    `from_address` VARCHAR(255) DEFAULT NULL COMMENT '起点地址',
    `to_address` VARCHAR(255) DEFAULT NULL COMMENT '终点地址',
    `distance` DECIMAL(10,2) DEFAULT 0 COMMENT '距离(公里)',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-待分配，1-已分配，2-运输中，3-已完成，4-已取消',
    `plan_start_time` DATETIME DEFAULT NULL COMMENT '计划开始时间',
    `plan_end_time` DATETIME DEFAULT NULL COMMENT '计划结束时间',
    `actual_start_time` DATETIME DEFAULT NULL COMMENT '实际开始时间',
    `actual_end_time` DATETIME DEFAULT NULL COMMENT '实际结束时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_no` (`task_no`),
    KEY `idx_driver_id` (`driver_id`),
    KEY `idx_vehicle_id` (`vehicle_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运输任务表';

-- ----------------------------
-- 库存调整记录表
-- ----------------------------
DROP TABLE IF EXISTS `wms_inventory_adjust`;
CREATE TABLE `wms_inventory_adjust` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `adjust_no` VARCHAR(50) NOT NULL COMMENT '调整单号',
    `inventory_id` BIGINT NOT NULL COMMENT '库存ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_code` VARCHAR(50) DEFAULT NULL COMMENT '商品编码',
    `product_name` VARCHAR(100) DEFAULT NULL COMMENT '商品名称',
    `warehouse_id` BIGINT NOT NULL COMMENT '库房ID',
    `before_quantity` INT DEFAULT 0 COMMENT '调整前数量',
    `adjust_quantity` INT DEFAULT 0 COMMENT '调整数量',
    `after_quantity` INT DEFAULT 0 COMMENT '调整后数量',
    `adjust_type` VARCHAR(20) DEFAULT NULL COMMENT '调整类型：GAIN-盘盈，LOSS-盘亏，DAMAGE-报损，OTHER-其他',
    `reason` VARCHAR(500) DEFAULT NULL COMMENT '调整原因',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作员ID',
    `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作员姓名',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_inventory_id` (`inventory_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存调整记录表';

-- ----------------------------
-- 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `operation` VARCHAR(100) DEFAULT NULL COMMENT '操作描述',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    `params` TEXT DEFAULT NULL COMMENT '请求参数',
    `result` TEXT DEFAULT NULL COMMENT '返回结果',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `duration` INT DEFAULT 0 COMMENT '执行时长(毫秒)',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =============================================
-- 初始化数据
-- =============================================

-- 初始化角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`) VALUES
(1, '系统管理员', 'ADMIN', '系统管理员，拥有所有权限', 1),
(2, '仓库管理员', 'WAREHOUSE_ADMIN', '仓库管理员，管理库房、库存等', 1),
(3, '操作员', 'OPERATOR', '操作员，执行出入库操作', 1);

-- 初始化用户（密码使用BCrypt加密，所有账号密码均为：admin123）
-- BCrypt编码 for "admin123": $2a$10$N9qo8uLOickgx2ZMRZoMyeR5hzqZJSFyy2j8p8RDXNvQpUMwgRhT2
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `status`) VALUES
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeR5hzqZJSFyy2j8p8RDXNvQpUMwgRhT2', '系统管理员', '13800000001', 'admin@wms.com', 1),
(2, 'warehouse', '$2a$10$N9qo8uLOickgx2ZMRZoMyeR5hzqZJSFyy2j8p8RDXNvQpUMwgRhT2', '仓库管理员', '13800000002', 'warehouse@wms.com', 1),
(3, 'operator', '$2a$10$N9qo8uLOickgx2ZMRZoMyeR5hzqZJSFyy2j8p8RDXNvQpUMwgRhT2', '操作员', '13800000003', 'operator@wms.com', 1);

-- 初始化用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 2),
(3, 3);

-- 初始化权限菜单
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `path`, `component`, `icon`, `type`, `sort`) VALUES
(1, 0, '系统管理', 'system', '/system', 'Layout', 'Setting', 1, 100),
(2, 1, '用户管理', 'system:user', '/system/user', 'system/user/index', 'User', 1, 101),
(3, 1, '角色管理', 'system:role', '/system/role', 'system/role/index', 'UserFilled', 1, 102),
(10, 0, '库房管理', 'warehouse', '/warehouse', 'Layout', 'House', 1, 10),
(11, 10, '库房列表', 'warehouse:list', '/warehouse/list', 'warehouse/list/index', 'OfficeBuilding', 1, 11),
(12, 10, '区域管理', 'warehouse:area', '/warehouse/area', 'warehouse/area/index', 'Grid', 1, 12),
(13, 10, '货架管理', 'warehouse:shelf', '/warehouse/shelf', 'warehouse/shelf/index', 'Box', 1, 13),
(14, 10, '库存监控', 'warehouse:inventory', '/warehouse/inventory', 'warehouse/inventory/index', 'DataAnalysis', 1, 14),
(20, 0, '出入库管理', 'stock', '/stock', 'Layout', 'Tickets', 1, 20),
(21, 20, '商品管理', 'stock:product', '/stock/product', 'stock/product/index', 'Goods', 1, 21),
(22, 20, '入库管理', 'stock:inbound', '/stock/inbound', 'stock/inbound/index', 'Download', 1, 22),
(23, 20, '出库管理', 'stock:outbound', '/stock/outbound', 'stock/outbound/index', 'Upload', 1, 23),
(24, 20, '库存调整', 'stock:adjust', '/stock/adjust', 'stock/adjust/index', 'Edit', 1, 24),
(30, 0, '策略管理', 'strategy', '/strategy', 'Layout', 'SetUp', 1, 30),
(31, 30, '存储策略', 'strategy:storage', '/strategy/storage', 'strategy/storage/index', 'FolderOpened', 1, 31),
(32, 30, '拣货策略', 'strategy:picking', '/strategy/picking', 'strategy/picking/index', 'Select', 1, 32),
(33, 30, '补货策略', 'strategy:replenish', '/strategy/replenish', 'strategy/replenish/index', 'Refresh', 1, 33),
(40, 0, '运输调度', 'transport', '/transport', 'Layout', 'Van', 1, 40),
(41, 40, '运输任务', 'transport:task', '/transport/task', 'transport/task/index', 'List', 1, 41),
(42, 40, '司机管理', 'transport:driver', '/transport/driver', 'transport/driver/index', 'Avatar', 1, 42),
(43, 40, '车辆管理', 'transport:vehicle', '/transport/vehicle', 'transport/vehicle/index', 'Truck', 1, 43),
(50, 0, '报表管理', 'report', '/report', 'Layout', 'DataLine', 1, 50),
(51, 50, '库存报表', 'report:inventory', '/report/inventory', 'report/inventory/index', 'PieChart', 1, 51),
(52, 50, '出入库报表', 'report:stock', '/report/stock', 'report/stock/index', 'TrendCharts', 1, 52),
(53, 50, '周转分析', 'report:turnover', '/report/turnover', 'report/turnover/index', 'DataAnalysis', 1, 53),
(54, 50, '异常预警', 'report:warning', '/report/warning', 'report/warning/index', 'WarnTriangleFilled', 1, 54),
(60, 0, '日志管理', 'log', '/log', 'Layout', 'Document', 1, 60),
(61, 60, '操作日志', 'log:operation', '/log/operation', 'log/operation/index', 'Memo', 1, 61);

-- 初始化角色权限关联（管理员拥有所有权限）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) 
SELECT 1, id FROM `sys_permission`;

-- 仓库管理员权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2, 10), (2, 11), (2, 12), (2, 13), (2, 14),
(2, 20), (2, 21), (2, 22), (2, 23), (2, 24),
(2, 30), (2, 31), (2, 32), (2, 33),
(2, 50), (2, 51), (2, 52), (2, 53), (2, 54);

-- 操作员权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(3, 10), (3, 14),
(3, 20), (3, 22), (3, 23);

-- 初始化示例库房
INSERT INTO `wms_warehouse` (`id`, `warehouse_code`, `warehouse_name`, `address`, `total_area`, `used_area`, `manager_name`, `phone`, `status`) VALUES
(1, 'WH001', '主仓库', '上海市浦东新区张江高科技园区', 10000.00, 6500.00, '张三', '13800000010', 1),
(2, 'WH002', '北京分仓', '北京市朝阳区望京科技园', 8000.00, 4200.00, '李四', '13800000011', 1);

-- 初始化示例区域
INSERT INTO `wms_warehouse_area` (`id`, `warehouse_id`, `area_code`, `area_name`, `area_type`, `area_size`, `status`) VALUES
(1, 1, 'A01', 'A区-存储区', 'STORAGE', 3000.00, 1),
(2, 1, 'B01', 'B区-拣货区', 'PICKING', 2000.00, 1),
(3, 1, 'C01', 'C区-收货区', 'RECEIVING', 1500.00, 1),
(4, 1, 'D01', 'D区-发货区', 'SHIPPING', 1500.00, 1),
(5, 2, 'A01', 'A区-存储区', 'STORAGE', 4000.00, 1),
(6, 2, 'B01', 'B区-拣货区', 'PICKING', 2000.00, 1);

-- 初始化示例货架
INSERT INTO `wms_shelf` (`id`, `area_id`, `warehouse_id`, `shelf_code`, `shelf_name`, `layer_count`, `column_count`, `max_weight`, `status`) VALUES
(1, 1, 1, 'A01-01', 'A区1号货架', 5, 10, 5000.00, 1),
(2, 1, 1, 'A01-02', 'A区2号货架', 5, 10, 5000.00, 1),
(3, 1, 1, 'A01-03', 'A区3号货架', 5, 10, 5000.00, 1),
(4, 2, 1, 'B01-01', 'B区1号货架', 3, 8, 3000.00, 1),
(5, 5, 2, 'A01-01', '北京A区1号货架', 5, 10, 5000.00, 1);

-- 初始化示例商品
INSERT INTO `wms_product` (`id`, `product_code`, `product_name`, `category`, `brand`, `unit`, `specification`, `weight`, `volume`, `price`, `min_stock`, `max_stock`, `status`) VALUES
(1, 'P001', '笔记本电脑', '电子产品', '联想', '台', '14英寸/i5/16G/512G', 2.000, 0.015, 5999.00, 10, 500, 1),
(2, 'P002', '无线鼠标', '电子配件', '罗技', '个', '蓝牙5.0', 0.100, 0.001, 199.00, 50, 2000, 1),
(3, 'P003', '机械键盘', '电子配件', '雷蛇', '个', '87键/青轴', 0.800, 0.003, 599.00, 30, 1000, 1),
(4, 'P004', '显示器', '电子产品', '戴尔', '台', '27英寸/2K', 5.000, 0.050, 2299.00, 20, 300, 1),
(5, 'P005', '打印纸', '办公用品', '得力', '箱', 'A4/500张/箱', 2.500, 0.008, 45.00, 100, 5000, 1);

-- 初始化示例库存
INSERT INTO `wms_inventory` (`product_id`, `product_code`, `product_name`, `warehouse_id`, `warehouse_name`, `area_id`, `shelf_id`, `location_code`, `batch_no`, `quantity`, `available_qty`, `locked_qty`, `production_date`, `expiry_date`) VALUES
(1, 'P001', '笔记本电脑', 1, '主仓库', 1, 1, 'A01-01-01', 'BN20240101', 100, 95, 5, '2024-01-01', '2027-01-01'),
(2, 'P002', '无线鼠标', 1, '主仓库', 1, 1, 'A01-01-02', 'BN20240102', 500, 480, 20, '2024-01-02', '2027-01-02'),
(3, 'P003', '机械键盘', 1, '主仓库', 1, 2, 'A01-02-01', 'BN20240103', 200, 200, 0, '2024-01-03', '2027-01-03'),
(4, 'P004', '显示器', 1, '主仓库', 1, 2, 'A01-02-02', 'BN20240104', 80, 75, 5, '2024-01-04', '2027-01-04'),
(5, 'P005', '打印纸', 1, '主仓库', 1, 3, 'A01-03-01', 'BN20240105', 1000, 980, 20, '2024-01-05', '2026-01-05'),
(1, 'P001', '笔记本电脑', 2, '北京分仓', 5, 5, 'A01-01-01', 'BN20240201', 50, 50, 0, '2024-02-01', '2027-02-01');

-- 初始化示例司机
INSERT INTO `wms_driver` (`id`, `driver_code`, `driver_name`, `phone`, `id_card`, `license_no`, `license_type`, `status`) VALUES
(1, 'DR001', '王师傅', '13900000001', '310101198001011234', 'D310101198001011234', 'A2', 1),
(2, 'DR002', '李师傅', '13900000002', '310101198502022345', 'D310101198502022345', 'B2', 1),
(3, 'DR003', '赵师傅', '13900000003', '110101199003033456', 'D110101199003033456', 'B2', 1);

-- 初始化示例车辆
INSERT INTO `wms_vehicle` (`id`, `plate_no`, `vehicle_type`, `brand`, `model`, `load_capacity`, `volume`, `purchase_date`, `status`) VALUES
(1, '沪A12345', '厢式货车', '江淮', 'JAC-K5', 5000.00, 20.00, '2022-01-01', 1),
(2, '沪B23456', '厢式货车', '福田', 'FOTON-M3', 8000.00, 35.00, '2021-06-01', 1),
(3, '京A34567', '小型货车', '五菱', 'WULING-N1', 2000.00, 8.00, '2023-03-01', 1);

-- 初始化示例策略
INSERT INTO `wms_storage_strategy` (`id`, `strategy_name`, `strategy_code`, `warehouse_id`, `category`, `rule_type`, `rule_content`, `priority`, `status`) VALUES
(1, '电子产品存储策略', 'SS001', 1, '电子产品', 'FIFO', '{"temperature": "15-25", "humidity": "40-60", "stackable": false}', 1, 1),
(2, '办公用品存储策略', 'SS002', 1, '办公用品', 'FIFO', '{"stackable": true, "maxStack": 5}', 2, 1);

INSERT INTO `wms_picking_strategy` (`id`, `strategy_name`, `strategy_code`, `warehouse_id`, `picking_type`, `rule_content`, `priority`, `status`) VALUES
(1, '单品拣货策略', 'PS001', 1, 'SINGLE', '{"maxItems": 1, "priority": "nearest"}', 1, 1),
(2, '批量拣货策略', 'PS002', 1, 'BATCH', '{"maxItems": 10, "priority": "route"}', 2, 1);

INSERT INTO `wms_replenish_strategy` (`id`, `strategy_name`, `strategy_code`, `warehouse_id`, `product_id`, `min_quantity`, `max_quantity`, `replenish_quantity`, `trigger_type`, `status`) VALUES
(1, '笔记本自动补货', 'RS001', 1, 1, 20, 200, 50, 'AUTO', 1),
(2, '鼠标自动补货', 'RS002', 1, 2, 100, 1000, 200, 'AUTO', 1);

SET FOREIGN_KEY_CHECKS = 1;
