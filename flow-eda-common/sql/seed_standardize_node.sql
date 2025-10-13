-- Insert node type 'standardize' if not exists
INSERT INTO eda_flow_node_type (id, type, type_name, menu, svg, background, description)
SELECT (SELECT COALESCE(MAX(id),0)+1 FROM eda_flow_node_type) AS id,
       'standardize','标准化参数','基础','/svg/splice.svg','rgb(200 180 75 / 60%)',
       '仅将上游自定义参数(input)向下游传递，不附带其他参数'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM eda_flow_node_type WHERE type='standardize');
