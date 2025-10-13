-- Insert node type 'generic_algo' if not exists
INSERT INTO eda_flow_node_type (id, type, type_name, menu, svg, background, description)
SELECT (SELECT COALESCE(MAX(id),0)+1 FROM eda_flow_node_type) AS id,
       'generic_algo','通用算法','算法','/svg/sequence.svg','rgb(200 180 75 / 60%)',
       '将输入参数空格拼接调用外部算法（feign，占位），返回后转为通用参数输出'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM eda_flow_node_type WHERE type='generic_algo');

-- Required param: algorithmId
INSERT INTO eda_flow_node_type_param (type_id, `key`, `name`, required, in_type, `option`, placeholder)
SELECT t.id, 'algorithmId', '算法ID', 1, 'input', NULL, 'algo-xxxx'
FROM eda_flow_node_type t
WHERE t.type='generic_algo'
  AND NOT EXISTS (
    SELECT 1 FROM eda_flow_node_type_param p WHERE p.type_id=t.id AND p.`key`='algorithmId'
  );
