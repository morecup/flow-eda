CREATE TABLE IF NOT EXISTS flow_instance (
    id               VARCHAR(64) PRIMARY KEY,
    flow_id          VARCHAR(64) NOT NULL,
    status           VARCHAR(32) NOT NULL,
    trigger_user     VARCHAR(64) NULL,
    terminate_reason VARCHAR(255) NULL,
    retry_count      INT DEFAULT 0,
    start_time       TIMESTAMP NULL,
    end_time         TIMESTAMP NULL,
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_flow_instance_flow_id ON flow_instance(flow_id);
CREATE INDEX IF NOT EXISTS idx_flow_instance_status ON flow_instance(status);

CREATE TABLE IF NOT EXISTS flow_instance_node (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id   VARCHAR(64) NOT NULL,
    node_id       VARCHAR(64) NOT NULL,
    node_name     VARCHAR(128) NULL,
    node_type     VARCHAR(64) NULL,
    status        VARCHAR(32) NOT NULL,
    duration_ms   BIGINT NULL,
    input_json    CLOB NULL,
    output_json   CLOB NULL,
    error_stack   CLOB NULL,
    start_time    TIMESTAMP NULL,
    end_time      TIMESTAMP NULL,
    created_at    TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_flow_instance_node_instance ON flow_instance_node(instance_id);
CREATE INDEX IF NOT EXISTS idx_flow_instance_node_status ON flow_instance_node(status);

CREATE TABLE IF NOT EXISTS flow_instance_log (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id  VARCHAR(64) NOT NULL,
    node_id      VARCHAR(64) NULL,
    level        VARCHAR(16) NOT NULL,
    category     VARCHAR(64) NULL,
    message      VARCHAR(1024) NULL,
    payload_json CLOB NULL,
    log_time     TIMESTAMP NOT NULL,
    created_at   TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_flow_instance_log_instance ON flow_instance_log(instance_id);
CREATE INDEX IF NOT EXISTS idx_flow_instance_log_level ON flow_instance_log(level);
