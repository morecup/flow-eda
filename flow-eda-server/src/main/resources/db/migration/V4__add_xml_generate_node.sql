-- 新增 XML 生成节点类型
-- 基于 XML 模板生成新的 XML 内容，并支持通过参数覆盖模板中的节点值或属性值

-- 1. 添加节点类型（分类为"解析"）
INSERT INTO flow_node_type (type, type_name, menu, svg, background, description)
SELECT 'xml_generate', 'XML生成', '解析', '/svg/xml_generate.svg', 'rgb(180 197 125 / 60%)',
       '基于xml模板生成新的xml内容，并支持参数覆盖'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flow_node_type WHERE type = 'xml_generate');

-- 2. 添加节点参数配置
-- template 输入框
INSERT INTO flow_node_type_param (type_id, `key`, name, required, in_type, placeholder)
SELECT id, 'template', '模板文件', 1, 'input', 'a.xml / classpath:xml-template/a.xml / file:D:/path/a.xml'
FROM flow_node_type
WHERE type = 'xml_generate'
  AND NOT EXISTS (
    SELECT 1 FROM flow_node_type_param p
    WHERE p.type_id = flow_node_type.id AND p.`key` = 'template'
  );

-- overrides 输入框（json）
INSERT INTO flow_node_type_param (type_id, `key`, name, required, in_type, placeholder)
SELECT id, 'overrides', '覆盖参数', 0, 'input', '{"Header.To":"Bob","Body.@id":"2"}'
FROM flow_node_type
WHERE type = 'xml_generate'
  AND NOT EXISTS (
    SELECT 1 FROM flow_node_type_param p
    WHERE p.type_id = flow_node_type.id AND p.`key` = 'overrides'
  );
