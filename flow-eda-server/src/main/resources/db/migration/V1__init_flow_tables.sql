-- Flow EDA Web服务表结构
-- 流程定义相关表

-- 流程定义表
CREATE TABLE IF NOT EXISTS flow_definition (
    id VARCHAR(64) PRIMARY KEY COMMENT '流程ID',
    name VARCHAR(128) NOT NULL COMMENT '流程名称',
    description VARCHAR(512) COMMENT '流程描述',
    username VARCHAR(64) COMMENT '创建用户',
    status VARCHAR(32) NOT NULL DEFAULT 'INIT' COMMENT '流程状态: INIT/RUNNING/FINISHED/FAILED',
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义表';

-- 节点类型表
CREATE TABLE IF NOT EXISTS flow_node_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '节点类型ID',
    type VARCHAR(64) NOT NULL COMMENT '节点类型',
    type_name VARCHAR(128) NOT NULL COMMENT '节点类型名称',
    menu VARCHAR(64) COMMENT '菜单分类',
    svg TEXT COMMENT '节点图标SVG',
    background VARCHAR(32) COMMENT '节点背景色',
    description VARCHAR(512) COMMENT '节点类型描述',
    UNIQUE INDEX uk_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点类型表';

-- 节点类型参数表
CREATE TABLE IF NOT EXISTS flow_node_type_param (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参数ID',
    type_id BIGINT NOT NULL COMMENT '节点类型ID',
    param_name VARCHAR(128) NOT NULL COMMENT '参数名称',
    param_key VARCHAR(64) NOT NULL COMMENT '参数键',
    param_type VARCHAR(32) NOT NULL COMMENT '参数类型',
    default_value VARCHAR(256) COMMENT '默认值',
    required TINYINT(1) DEFAULT 0 COMMENT '是否必填',
    description VARCHAR(512) COMMENT '参数描述',
    INDEX idx_type_id (type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点类型参数表';

-- 节点数据表
CREATE TABLE IF NOT EXISTS flow_node_data (
    id VARCHAR(64) PRIMARY KEY COMMENT '节点数据ID',
    node_name VARCHAR(128) COMMENT '节点名称',
    flow_id VARCHAR(64) NOT NULL COMMENT '流程ID',
    type_id BIGINT NOT NULL COMMENT '节点类型ID',
    top VARCHAR(16) COMMENT '节点位置top',
    `left` VARCHAR(16) COMMENT '节点位置left',
    remark VARCHAR(512) COMMENT '节点备注',
    params TEXT COMMENT '节点参数JSON',
    payload TEXT COMMENT '自定义参数JSON',
    `from` VARCHAR(64) COMMENT '连线起始节点ID',
    `to` VARCHAR(64) COMMENT '连线结束节点ID',
    version VARCHAR(64) COMMENT '数据版本',
    INDEX idx_flow_id (flow_id),
    INDEX idx_type_id (type_id),
    INDEX idx_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点数据表';
