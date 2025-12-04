-- 添加入库节点类型
-- 数据入库节点：调用 RemoteDataIngestService.createAndExecuteTask 创建入库任务并同步等待完成

-- 1. 添加节点类型（分类为"算法"）
INSERT INTO flow_node_type (type, type_name, menu, svg, background, description)
VALUES ('data_ingest', '入库节点', '算法', '/svg/sequence.svg', 'rgb(120 150 90 / 42%)', '数据入库节点，调用入库服务创建入库任务并同步等待完成');

-- 2. 添加节点参数配置
-- dataType 下拉选择
INSERT INTO flow_node_type_param (type_id, `key`, name, required, in_type, `option`)
SELECT id, 'dataType', '数据类型', 1, 'select',
       'RAW_IMAGE,STANDARD_PRODUCT,REGIONAL_PRODUCT,THEMATIC_PRODUCT'
FROM flow_node_type WHERE type = 'data_ingest';

-- configId 输入框
INSERT INTO flow_node_type_param (type_id, `key`, name, required, in_type, placeholder)
SELECT id, 'configId', '入库配置ID', 1, 'input', '请输入入库配置树节点ID'
FROM flow_node_type WHERE type = 'data_ingest';

-- mappingId 输入框
INSERT INTO flow_node_type_param (type_id, `key`, name, required, in_type, placeholder)
SELECT id, 'mappingId', '元数据对照ID', 1, 'input', '请输入元数据对照配置ID'
FROM flow_node_type WHERE type = 'data_ingest';

-- ingestDataPath 输入框（可选）
INSERT INTO flow_node_type_param (type_id, `key`, name, required, in_type, placeholder)
SELECT id, 'ingestDataPath', '入库数据路径', 0, 'input', '可选，默认从上游获取'
FROM flow_node_type WHERE type = 'data_ingest';
