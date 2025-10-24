-- ====================================================
-- 数据库表名重构迁移脚本
-- 将所有 eda_ 前缀的表统一改为 flow_ 前缀
-- 执行时间: 2025-10-24
-- ====================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. 重命名流程定义表
-- eda_flow → flow_definition
RENAME TABLE `eda_flow` TO `flow_definition`;

-- 2. 重命名流程节点数据表
-- eda_flow_node_data → flow_node_data
RENAME TABLE `eda_flow_node_data` TO `flow_node_data`;

-- 3. 重命名流程节点类型表
-- eda_flow_node_type → flow_node_type
RENAME TABLE `eda_flow_node_type` TO `flow_node_type`;

-- 4. 重命名流程节点类型参数表
-- eda_flow_node_type_param → flow_node_type_param
RENAME TABLE `eda_flow_node_type_param` TO `flow_node_type_param`;
SET FOREIGN_KEY_CHECKS = 1;

-- ====================================================
-- 验证迁移结果
-- ====================================================
-- 执行以下SQL查询验证表是否成功重命名:
--
-- SHOW TABLES LIKE 'flow_%';
-- SHOW TABLES LIKE 'eda_%';
--
-- 预期结果:
-- - flow_definition
-- - flow_node_data
-- - flow_node_type
-- - flow_node_type_param
-- - flow_instance
-- - flow_instance_node
-- - flow_instance_log
--
-- eda_ 前缀的表应该不存在(除非是其他业务表)
-- ====================================================
