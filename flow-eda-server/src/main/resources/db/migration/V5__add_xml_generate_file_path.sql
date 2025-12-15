-- 为 XML 生成节点新增输出文件路径参数
-- filePath 不为空时，节点会将生成的 XML 写入该路径（UTF-8，覆盖写）

INSERT INTO flow_node_type_param (type_id, `key`, name, required, in_type, placeholder)
SELECT id, 'filePath', '输出文件路径', 0, 'input', 'D:/path/out.xml（可使用占位符参数filePath）'
FROM flow_node_type
WHERE type = 'xml_generate'
  AND NOT EXISTS (
    SELECT 1 FROM flow_node_type_param p
    WHERE p.type_id = flow_node_type.id AND p.`key` = 'filePath'
  );
