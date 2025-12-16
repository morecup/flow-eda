-- ----------------------------
-- Table structure for flow_node_type_param
-- 流程节点类型参数表
-- ----------------------------
DROP TABLE IF EXISTS `flow_node_type_param`;
CREATE TABLE `flow_node_type_param`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type_id` bigint NOT NULL COMMENT '节点类型id',
  `key` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '参数key',
  `name` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '参数名称',
  `required` tinyint(1) NULL DEFAULT 0 COMMENT '参数是否必填',
  `in_type` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '参数输入类型',
  `option` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '下拉选项内容，多个值以逗号分隔',
  `placeholder` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '参数值提示性内容',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_type_id`(`type_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 88 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of flow_node_type_param
-- ----------------------------
INSERT INTO `flow_node_type_param` VALUES (1, 2, 'period', '执行周期', 1, 'select', 'SECONDS,MINUTES,HOURS,DAYS,CRON', '1,MINUTES');
INSERT INTO `flow_node_type_param` VALUES (2, 2, 'times', '执行次数', 1, 'input', NULL, '输入0表示不限制次数');
INSERT INTO `flow_node_type_param` VALUES (3, 2, 'timestamp', '输出时间', 0, 'select', 'timestamp,yyyy-MM-dd HH:mm:ss,yyyy-MM-dd\'T\'HH:mm:ss\'Z\',yyyyMMdd,HHmmss,HH:mm:ss,yyyy-MM-dd,yyyy/MM/dd,yyyyMMddHHmmss', NULL);
INSERT INTO `flow_node_type_param` VALUES (4, 3, 'delay', '延迟时间', 1, 'select', 'MILLISECONDS,SECONDS,MINUTES,HOURS,DAYS', '5,SECONDS');
INSERT INTO `flow_node_type_param` VALUES (5, 21, 'parseKey', '解析参数', 1, 'input', NULL, 'httpResult.$0.name,params.a');
INSERT INTO `flow_node_type_param` VALUES (6, 31, 'url', 'URL', 1, 'input', NULL, '/api/http/test');
INSERT INTO `flow_node_type_param` VALUES (7, 31, 'method', '请求方式', 1, 'select', 'GET,POST,PUT,DELETE,HEAD,OPTIONS,TRACE,PATCH', NULL);
INSERT INTO `flow_node_type_param` VALUES (8, 31, 'params', '请求参数', 0, 'input', NULL, 'id=1&name=xx');
INSERT INTO `flow_node_type_param` VALUES (9, 31, 'body', '请求体', 0, 'input', NULL, '{\'id\': 1, \'name\': \'xx\'}');
INSERT INTO `flow_node_type_param` VALUES (10, 31, 'header', '请求头', 0, 'input', NULL, 'Authorization:xxx,Content-Type:(默认json)');
INSERT INTO `flow_node_type_param` VALUES (11, 33, 'uri', 'URI', 1, 'input', NULL, '/api/http/test');
INSERT INTO `flow_node_type_param` VALUES (12, 33, 'method', '请求方式', 1, 'select', 'GET,POST,PUT,DELETE,HEAD,OPTIONS,TRACE,PATCH', NULL);
INSERT INTO `flow_node_type_param` VALUES (14, 33, 'resData', '响应数据', 0, 'input', NULL, '{\"result\":\"OK\"} (不填默认返回上游节点的输出)');
INSERT INTO `flow_node_type_param` VALUES (22, 37, 'clientId', 'Client ID', 1, 'input', NULL, 'mqtt_xxxxxx');
INSERT INTO `flow_node_type_param` VALUES (23, 37, 'broker', '服务器地址', 1, 'select', '1883,8083,8883,8084', 'tcp://broker.emqx.io,1883');
INSERT INTO `flow_node_type_param` VALUES (24, 37, 'topic', 'Topic', 1, 'input', NULL, '/test/+/xx/#');
INSERT INTO `flow_node_type_param` VALUES (25, 37, 'username', '用户名', 0, 'input', NULL, NULL);
INSERT INTO `flow_node_type_param` VALUES (26, 37, 'password', '密码', 0, 'input', NULL, NULL);
INSERT INTO `flow_node_type_param` VALUES (27, 38, 'clientId', 'Client ID', 1, 'input', NULL, 'mqtt_xxxxxx');
INSERT INTO `flow_node_type_param` VALUES (28, 38, 'broker', '服务器地址', 1, 'select', '1883,8083,8883,8084', 'tcp://broker.emqx.io,1883');
INSERT INTO `flow_node_type_param` VALUES (29, 38, 'topic', 'Topic', 1, 'input', NULL, '/test/xx');
INSERT INTO `flow_node_type_param` VALUES (30, 38, 'message', '发送消息', 1, 'input', NULL, NULL);
INSERT INTO `flow_node_type_param` VALUES (31, 38, 'username', '用户名', 0, 'input', NULL, NULL);
INSERT INTO `flow_node_type_param` VALUES (32, 38, 'password', '密码', 0, 'input', NULL, NULL);
INSERT INTO `flow_node_type_param` VALUES (33, 51, 'url', 'URL地址', 1, 'input', NULL, 'jdbc:mysql://127.0.0.1:3306/flow_eda_test');
INSERT INTO `flow_node_type_param` VALUES (34, 51, 'username', '用户名', 1, 'input', NULL, 'root');
INSERT INTO `flow_node_type_param` VALUES (35, 51, 'password', '密码', 1, 'input', NULL, NULL);
INSERT INTO `flow_node_type_param` VALUES (36, 51, 'sql', '执行SQL语句', 1, 'input', NULL, '支持单条或多条sql语句，多条语句之间使用分号相隔');
INSERT INTO `flow_node_type_param` VALUES (37, 52, 'url', 'URL地址', 1, 'input', NULL, 'mongodb://root:admin@127.0.0.1:27017/admin');
INSERT INTO `flow_node_type_param` VALUES (38, 52, 'db', '数据库名称', 1, 'input', NULL, NULL);
INSERT INTO `flow_node_type_param` VALUES (39, 52, 'command', '执行语句', 1, 'input', NULL, '{\'find\': \'test_c\', \'filter\': {\'name\': \'test\'}, \'limit\': 1}');
INSERT INTO `flow_node_type_param` VALUES (40, 53, 'uri', '服务器地址', 1, 'input', NULL, '127.0.0.1:6379');
INSERT INTO `flow_node_type_param` VALUES (41, 53, 'method', '操作', 1, 'select', 'set,get,del,exists,getSet,getDel,hset,hget,hdel,hgetAll,hexists', NULL);
INSERT INTO `flow_node_type_param` VALUES (42, 53, 'args', '参数', 1, 'input', NULL, 'testxx,Hello World! (多个参数用,分隔)');
INSERT INTO `flow_node_type_param` VALUES (43, 53, 'username', '用户名', 0, 'input', NULL, NULL);
INSERT INTO `flow_node_type_param` VALUES (44, 53, 'password', '密码', 0, 'input', NULL, NULL);
INSERT INTO `flow_node_type_param` VALUES (45, 53, 'database', '数据库', 0, 'input', NULL, '1');
INSERT INTO `flow_node_type_param` VALUES (46, 5, 'field', '字段名', 1, 'input', NULL, '仅可填写单个字段名');
INSERT INTO `flow_node_type_param` VALUES (47, 5, 'condition', '判断逻辑', 1, 'select', '=,!=,>,>=,<,<=', NULL);
INSERT INTO `flow_node_type_param` VALUES (48, 5, 'value', '判断值', 1, 'input', NULL, '支持null，表示该字段不存在或值为null');
INSERT INTO `flow_node_type_param` VALUES (49, 32, 'uri', 'URI', 1, 'input', NULL, '/api/http/test');
INSERT INTO `flow_node_type_param` VALUES (50, 32, 'method', '请求方式', 1, 'select', 'GET,POST,PUT,DELETE,HEAD,OPTIONS,TRACE,PATCH', NULL);
INSERT INTO `flow_node_type_param` VALUES (51, 6, 'start', '初始值', 1, 'input', NULL, '请输入整数（产生的序列包含此值）');
INSERT INTO `flow_node_type_param` VALUES (52, 6, 'end', '结束值', 1, 'input', NULL, '请输入整数（产生的序列包含此值）');
INSERT INTO `flow_node_type_param` VALUES (53, 6, 'action', '递进方式(默认递增)', 0, 'select', '递增,递减', NULL);
INSERT INTO `flow_node_type_param` VALUES (54, 6, 'step', '递进间隔', 0, 'input', NULL, '请输入大于0的正整数（默认值为1）');
INSERT INTO `flow_node_type_param` VALUES (55, 7, 'field', '字段名', 1, 'input', NULL, NULL);
INSERT INTO `flow_node_type_param` VALUES (56, 7, 'filter', '是否过滤空值（默认过滤）', 0, 'select', '过滤,不过滤', NULL);
INSERT INTO `flow_node_type_param` VALUES (57, 8, 'field', '字段名', 1, 'input', NULL, NULL);
INSERT INTO `flow_node_type_param` VALUES (58, 8, 'separator', '分割符', 0, 'input', NULL, '默认以,分割');
INSERT INTO `flow_node_type_param` VALUES (59, 8, 'outputWay', '输出方式（默认输出数组）', 0, 'select', '单次输出数组,多次输出单值', NULL);
INSERT INTO `flow_node_type_param` VALUES (60, 100, 'subflow', '选择子流程', 1, 'api', NULL, NULL);
INSERT INTO `flow_node_type_param` VALUES (61, 22, 'parseKey', '解析参数（html来源）', 1, 'input', NULL, 'httpResult.html');
INSERT INTO `flow_node_type_param` VALUES (62, 22, 'selector', 'CSS选择器', 1, 'select', 'class,tag,id', 'x-xx (className),class');
INSERT INTO `flow_node_type_param` VALUES (63, 22, 'filterTags', '二次过滤标签', 0, 'input', NULL, 'div');
INSERT INTO `flow_node_type_param` VALUES (64, 22, 'outputType', '输出内容（默认输出文本内容）', 0, 'select', '输出文本内容,输出html内容', NULL);
INSERT INTO `flow_node_type_param` VALUES (65, 23, 'parseKey', '解析参数（xml来源）', 1, 'input', NULL, 'httpResult.xml');
INSERT INTO `flow_node_type_param` VALUES (66, 23, 'attr', '标签属性名称', 0, 'input', NULL, '默认为@attributes');
INSERT INTO `flow_node_type_param` VALUES (84, 24, 'template', '模板文件', 1, 'input', NULL, 'a.xml / classpath:xml-template/a.xml / file:D:/path/a.xml');
INSERT INTO `flow_node_type_param` VALUES (85, 24, 'overrides', '覆盖参数', 0, 'input', NULL, '{"Header.To":"Bob","Body.@id":"2"}');
INSERT INTO `flow_node_type_param` VALUES (86, 24, 'filePath', '输出文件路径', 0, 'input', NULL, 'D:/path/out.xml（可使用占位符参数filePath）');
INSERT INTO `flow_node_type_param` VALUES (67, 39, 'host', '邮件服务器', 1, 'input', NULL, 'smtp.qq.com');
INSERT INTO `flow_node_type_param` VALUES (68, 39, 'fromEmail', '发件人邮箱', 1, 'input', NULL, 'xxx@qq.com');
INSERT INTO `flow_node_type_param` VALUES (69, 39, 'authCode', '邮箱授权码', 1, 'input', NULL, '邮箱开启SMTP服务会有一个授权码');
INSERT INTO `flow_node_type_param` VALUES (70, 39, 'toEmail', '收件人邮箱', 1, 'input', NULL, '多个邮箱之间使用,分隔');
INSERT INTO `flow_node_type_param` VALUES (71, 39, 'subject', '邮件主题', 1, 'input', NULL, '填写邮件的主题/标题');
INSERT INTO `flow_node_type_param` VALUES (72, 39, 'text', '邮件正文', 1, 'input', NULL, '填写邮件的正文内容，支持html等格式');
INSERT INTO `flow_node_type_param` VALUES (73, 39, 'isHtml', '发送格式（默认文本格式）', 0, 'select', '文本格式,HTML格式', NULL);
INSERT INTO `flow_node_type_param` VALUES (74, 39, 'ccEmail', '抄送邮箱', 0, 'input', NULL, '多个邮箱之间使用,分隔');
INSERT INTO `flow_node_type_param` VALUES (75, 39, 'bccEmail', '密送邮箱', 0, 'input', NULL, '多个邮箱之间使用,分隔');
INSERT INTO `flow_node_type_param` VALUES (76, 54, 'url', 'URL地址', 1, 'input', NULL, 'jdbc:postgresql://localhost:5432/postgres');
INSERT INTO `flow_node_type_param` VALUES (77, 54, 'username', '用户名', 1, 'input', NULL, 'postgres');
INSERT INTO `flow_node_type_param` VALUES (78, 54, 'password', '密码', 1, 'input', NULL, NULL);
INSERT INTO `flow_node_type_param` VALUES (79, 54, 'sql', '执行SQL语句', 1, 'input', NULL, '支持单条或多条sql语句，多条语句之间使用分号相隔');
INSERT INTO `flow_node_type_param` VALUES (80, 9, 'times', '执行次数', 0, 'input', NULL, '默认1');
INSERT INTO `flow_node_type_param` VALUES (81, 9, 'period', '限制周期（单位：ms）', 0, 'input', NULL, '默认100');
INSERT INTO `flow_node_type_param` VALUES (82, 10, 'inputTifPath', 'TIF路径', 0, 'input', NULL, 'D:/path/to/file.tif');
INSERT INTO `flow_node_type_param` VALUES (83, 12, 'algorithmId', '算法ID', 1, 'input', NULL, 'algo-xxxx');
INSERT INTO `flow_node_type_param` VALUES (87, 12, 'algorithmInput', '算法输入', 1, 'input', NULL, '直接透传给算法 jobParam，支持${}占位符');
