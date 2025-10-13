-- Insert node type 'tif_path' if not exists; id uses max(id)+1
INSERT INTO eda_flow_node_type (id, type, type_name, menu, svg, background, description)
SELECT (SELECT COALESCE(MAX(id),0)+1 FROM eda_flow_node_type) AS id,
       'tif_path','TIF路径','算法','/svg/sequence.svg','rgb(200 180 75 / 60%)',
       '根据输入的 inputTifPath，输出父级目录到 outputTifPath'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM eda_flow_node_type WHERE type='tif_path');

-- Add optional param definition for inputTifPath if not exists
INSERT INTO eda_flow_node_type_param (type_id, `key`, `name`, required, in_type, `option`, placeholder)
SELECT t.id, 'inputTifPath', 'TIF路径', 0, 'input', NULL, 'D:/path/to/file.tif'
FROM eda_flow_node_type t
WHERE t.type='tif_path'
  AND NOT EXISTS (
      SELECT 1 FROM eda_flow_node_type_param p WHERE p.type_id=t.id AND p.`key`='inputTifPath'
  );
