-- ----------------------------
-- Table structure for flow_node_type
-- 流程节点类型表
-- ----------------------------
DROP TABLE IF EXISTS `flow_node_type`;
CREATE TABLE `flow_node_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '节点类型',
  `type_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '名称',
  `menu` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '节点菜单分类',
  `svg` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图标',
  `background` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '背景色',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 103 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of flow_node_type
-- ----------------------------
INSERT INTO `flow_node_type` VALUES (1, 'start', '开始', '基础', '/svg/start.svg', 'rgb(0 128 0 / 20%)', '在一个流程中，开始节点是触发流程执行的起始节点');
INSERT INTO `flow_node_type` VALUES (2, 'timer', '定时器', '基础', '/svg/timer.svg', 'rgb(0 128 88 / 25%)', '可作为起始节点，用于定时触发流程周期性执行，可指定执行次数，可输出指定格式的时间戳；亦可作为非起始节点，由上游节点触发执行。');
INSERT INTO `flow_node_type` VALUES (3, 'delay', '延时器', '基础', '/svg/delay.svg', 'rgb(8 155 16 / 25%)', '延时节点，可设定延迟时间，用于延迟其下游节点的运行');
INSERT INTO `flow_node_type` VALUES (4, 'output', '输出', '基础', '/svg/output.svg', 'rgb(145 188 130 / 75%)', '输出节点，可展示其上游节点的输出参数信息');
INSERT INTO `flow_node_type` VALUES (5, 'condition', '条件', '运算', '/svg/condition.svg', 'rgb(220 200 80 / 60%)', '条件节点，用于判断条件是否满足，可使用多个此节点组成与或等逻辑通路，条件满足后会继续向下游节点执行输出');
INSERT INTO `flow_node_type` VALUES (6, 'sequence', '序列', '运算', '/svg/sequence.svg', 'rgb(170 150 50 / 40%)', '序列节点，可按自定义间隔大小进行递增或递减，依次输出整数序列');
INSERT INTO `flow_node_type` VALUES (7, 'splice', '拼接', '运算', '/svg/splice.svg', 'rgb(200 180 75 / 60%)', '拼接节点，在一定时间内持续接收上游节点的输出参数，对指定字段的值进行拼接并输出');
INSERT INTO `flow_node_type` VALUES (8, 'split', '分割', '运算', '/svg/split.svg', 'rgb(180 150 20 / 50%)', '分割节点，对指定字段的值进行分割后输出，可指定输出方式');
INSERT INTO `flow_node_type` VALUES (9, 'valve', '阀门', '运算', '/svg/valve.svg', 'rgb(230 210 70 / 70%)', '阀门节点，可用于多条支路汇总时，限制某一段时间周期内，通过此节点的次数。默认100毫秒内仅允许通过一次');
INSERT INTO `flow_node_type` VALUES (10, 'tif_path', 'TIF路径', '算法', '/svg/sequence.svg', 'rgb(200 180 75 / 60%)', '根据输入的 inputTifPath，输出父级目录到 outputTifPath');
INSERT INTO `flow_node_type` VALUES (11, 'standardize', '标准化参数', '基础', '/svg/splice.svg', 'rgb(200 180 75 / 60%)', '仅将上游自定义参数(input)向下游传递，不附带其他参数');
INSERT INTO `flow_node_type` VALUES (12, 'generic_algo', '通用算法', '算法', '/svg/sequence.svg', 'rgb(200 180 75 / 60%)', '使用algorithmInput作为jobParam调用外部算法（支持${}占位符），并透传上游input继续向下游传递');
INSERT INTO `flow_node_type` VALUES (21, 'json_parser', 'JSON解析', '解析', '/svg/json_parser.svg', 'rgb(180 197 125 / 60%)', '用于解析json格式的内容，可解析上游节点的输出参数，获取用户需要的参数信息');
INSERT INTO `flow_node_type` VALUES (22, 'html_parser', 'HTML解析', '解析', '/svg/html_parser.svg', 'rgb(180 197 125 / 60%)', '用于解析html格式的文本内容，可指定css选择器解析出目标元素，输出html内容或文本内容');
INSERT INTO `flow_node_type` VALUES (23, 'xml_parser', 'XML解析', '解析', '/svg/xml_parser.svg', 'rgb(180 197 125 / 60%)', '用于解析xml格式的文本内容，可转化为json格式的参数信息输出');
INSERT INTO `flow_node_type` VALUES (24, 'xml_generate', 'XML生成', '解析', '/svg/xml_generate.svg', 'rgb(180 197 125 / 60%)', '基于xml模板生成新的xml内容，并支持参数覆盖');
INSERT INTO `flow_node_type` VALUES (31, 'http_request', 'HTTP请求', '网络', '/svg/http_request.svg', 'rgb(235 186 73 / 75%)', '可以发起HTTP请求，支持所有的请求类型，支持携带各种请求参数信息以及token等请求头信息');
INSERT INTO `flow_node_type` VALUES (32, 'http_receive', 'HTTP接收', '网络', '/svg/http_receive.svg', 'rgb(235 186 73 / 75%)', '可创建HTTP服务，用于接收HTTP请求，解析出请求参数，向下游输出。可根据请求参数处理业务逻辑，与HTTP响应节点搭配使用');
INSERT INTO `flow_node_type` VALUES (33, 'http_response', 'HTTP响应', '网络', '/svg/http_response.svg', 'rgb(200 195 60 / 70%)', '可响应HTTP请求，与HTTP接收节点搭配使用，在处理完业务逻辑后，响应请求并返回数据，可根据上游节点的输出结果动态响应数据');
INSERT INTO `flow_node_type` VALUES (37, 'mqtt_sub', 'MQTT订阅', '网络', '/svg/mqtt_sub.svg', 'rgb(140 180 40 / 50%)', 'MQTT订阅节点，可订阅指定topic中的消息，接收到消息后会向下游输出。本节点为阻塞节点，运行后需要手动停止');
INSERT INTO `flow_node_type` VALUES (38, 'mqtt_pub', 'MQTT发布', '网络', '/svg/mqtt_pub.svg', 'rgb(130 160 50 / 60%)', 'MQTT发布节点，可向指定topic中发送MQTT消息，发送的消息内容会向下游节点输出');
INSERT INTO `flow_node_type` VALUES (39, 'email', '发送邮件', '网络', '/svg/email.svg', 'rgb(220 190 110 / 80%)', '可以发送电子邮件，支持抄送、密送，支持发送html格式的邮件');
INSERT INTO `flow_node_type` VALUES (51, 'mysql', 'Mysql', '数据库', '/svg/mysql.svg', 'rgb(220 180 50 / 50%)', 'Mysql节点，可连接mysql数据库，执行自定义sql语句，支持任意类型的多条sql语句，输出每条语句的执行结果和内容');
INSERT INTO `flow_node_type` VALUES (52, 'mongodb', 'MongoDB', '数据库', '/svg/mongodb.svg', 'rgb(230 170 60 / 70%)', 'MongoDB节点，可连接MongoDB数据库，执行自定义命令语句，输出执行结果和内容');
INSERT INTO `flow_node_type` VALUES (53, 'redis', 'Redis', '数据库', '/svg/redis.svg', 'rgb(235 180 10 / 50%)', 'Redis节点，可连接redis服务器，执行自定义操作，输出执行结果和内容');
INSERT INTO `flow_node_type` VALUES (54, 'postgresql', 'PostgreSQL', '数据库', '/svg/postgresql.svg', 'rgb(220 180 50 / 50%)', 'PostgreSQL节点，可连接PostgreSQL数据库，执行自定义sql语句，支持任意类型的多条sql语句，输出每条语句的执行结果和内容');
INSERT INTO `flow_node_type` VALUES (100, 'subflow', '子流程', '子流程', '/svg/subflow.svg', 'rgb(200 70 180 / 62%)', '子流程节点，可选择其他流程作为子流程来执行，本节点的输入参数可传递至子流程中的[子输入节点]，子流程中的[子输出节点]可将输出参数返回至本节点作为输出参数');
INSERT INTO `flow_node_type` VALUES (101, 'sub_input', '子输入', '子流程', '/svg/sub_input.svg', 'rgb(190 70 50 / 42%)', '子流程输入节点，用于子流程中，可接收关联的[子流程节点]的输入参数');
INSERT INTO `flow_node_type` VALUES (102, 'sub_output', '子输出', '子流程', '/svg/sub_output.svg', 'rgb(120 150 90 / 42%)', '子流程输出节点，用于子流程中，可将本节点的输出参数传递给关联的[子流程节点]作为输出参数');
