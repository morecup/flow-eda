-- 为 flow_instance_node 表添加唯一索引,确保同一实例的同一节点只有一条记录
ALTER TABLE flow_instance_node ADD UNIQUE INDEX idx_instance_node_unique (instance_id, node_id);
