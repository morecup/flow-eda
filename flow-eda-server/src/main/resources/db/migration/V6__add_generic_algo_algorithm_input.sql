-- 为通用算法节点补充算法输入参数
-- algorithmInput：直接透传给算法 jobParam（支持占位符替换，语法示例：$ {key}）

-- 1. 确保节点类型存在（分类为"算法"）
INSERT INTO flow_node_type (type, type_name, menu, svg, background, description)
SELECT 'generic_algo', '通用算法', '算法', '/svg/sequence.svg', 'rgb(200 180 75 / 60%)',
       CONCAT('使用algorithmInput作为jobParam调用外部算法（支持占位符替换，示例：', '$', '{key}', '），并透传上游input继续向下游传递')
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node_type WHERE type = 'generic_algo');

-- 2. 确保 algorithmId 参数存在
INSERT INTO flow_node_type_param (type_id, `key`, name, required, in_type, placeholder)
SELECT id, 'algorithmId', '算法ID', 1, 'input', 'algo-xxxx'
FROM flow_node_type
WHERE type = 'generic_algo'
  AND NOT EXISTS (
    SELECT 1 FROM flow_node_type_param p
    WHERE p.type_id = flow_node_type.id AND p.`key` = 'algorithmId'
  );

-- 3. 确保 algorithmInput 参数存在
INSERT INTO flow_node_type_param (type_id, `key`, name, required, in_type, placeholder)
SELECT id, 'algorithmInput', '算法输入', 1, 'input',
       CONCAT('直接透传给算法 jobParam，支持占位符替换（示例：', '$', '{key}', '）')
FROM flow_node_type
WHERE type = 'generic_algo'
  AND NOT EXISTS (
    SELECT 1 FROM flow_node_type_param p
    WHERE p.type_id = flow_node_type.id AND p.`key` = 'algorithmInput'
  );

-- 4. 如果已存在 algorithmInput，统一设为必填
UPDATE flow_node_type_param p
JOIN flow_node_type t ON p.type_id = t.id
SET p.required = 1
WHERE t.type = 'generic_algo'
  AND p.`key` = 'algorithmInput';
