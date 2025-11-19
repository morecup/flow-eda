-- Add job_id column to flow_instance_node table
ALTER TABLE `flow_instance_node` 
ADD COLUMN `job_id` BIGINT NULL COMMENT '外部任务ID' AFTER `node_type`;
