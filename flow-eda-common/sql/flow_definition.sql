-- ----------------------------
-- Table structure for flow_definition
-- 流程定义表
-- ----------------------------
DROP TABLE IF EXISTS `flow_definition`;
CREATE TABLE `flow_definition`  (
  `id` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'id',
  `name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '名称',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `username` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `status` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '状态',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_create_date`(`create_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of flow_definition
-- ----------------------------
INSERT INTO `flow_definition` VALUES ('13mwrf5znqv400', 'MQTT订阅消息示例', '订阅MQTT消息，输出接收到的消息内容', 'test', 'FINISHED', '2022-06-01 18:01:21', '2022-06-01 18:01:21');
INSERT INTO `flow_definition` VALUES ('1geaido0ecww00', '解析XML示例', '用于演示HTTP请求返回xml格式，以及XML格式内容的解析', 'test', 'FINISHED', '2022-05-12 17:57:10', '2022-10-19 17:57:28');
INSERT INTO `flow_definition` VALUES ('1p8nhh3c4aio00', '解析HTTP请求结果', '包含HTTP请求-解析器节点，解析请求结果并输出', 'test', 'FINISHED', '2022-05-09 16:03:39', '2022-05-09 16:03:39');
INSERT INTO `flow_definition` VALUES ('1pdye9g349ls00', '子流程示例-子流程', '用于演示嵌套流程的运行，本流程作为子流程', 'test', 'FINISHED', '2022-09-30 16:49:20', '2022-09-30 16:49:20');
INSERT INTO `flow_definition` VALUES ('28pm30nbgs2s00', 'MongoDB节点使用示例', '使用MongoDB节点执行自定义命令语句，输出执行结果。前置条件：需要具备或已知mongodb数据库地址，更新节点属性后再运行流程', 'test', 'FAILED', '2022-06-08 15:13:25', '2022-06-08 15:26:21');
INSERT INTO `flow_definition` VALUES ('2dzjjpdtijb400', '定时发起HTTP请求并附带输出时间', '定时器+HTTP请求+解析器+延时器，发起请求，延迟输出结果并附带请求时间', 'test', 'FINISHED', '2022-06-19 17:58:29', '2022-06-19 17:58:29');
INSERT INTO `flow_definition` VALUES ('2fnllj4jorfo00', '解析HTML示例', '用于演示HTTP请求返回html格式，以及HTML格式内容的解析', 'test', 'FINISHED', '2022-05-11 17:25:45', '2022-10-18 17:26:21');
INSERT INTO `flow_definition` VALUES ('34224hnftcm000', 'HTTP请求示例', '包含HTTP请求节点，发送网络请求，并输出响应结果', 'test', 'FINISHED', '2022-05-09 15:57:18', '2022-05-09 16:01:16');
INSERT INTO `flow_definition` VALUES ('395ecb7kvk8000', '发送邮件示例', '演示如何发送电子邮件，公开环境中如需测试，建议新建流程测试，请勿留下授权码以免被其他人利用', 'test', 'FINISHED', '2022-10-25 15:40:06', '2022-10-26 15:40:53');
INSERT INTO `flow_definition` VALUES ('3f1qhocnot6000', 'Redis节点使用示例', '使用Redis节点连接至指定的Redis服务器，执行自定义操作，查看执行结果和输出内容。前置条件：需要具备或已知Redis服务器地址，更新到节点属性上再运行流程', 'test', 'FAILED', '2022-06-08 17:20:44', '2022-06-08 17:22:24');
INSERT INTO `flow_definition` VALUES ('3gsaefsrjvc000', 'MQTT发布消息示例', '自定义发布消息到指定的MQTT中，并输出发送的消息内容', 'test', 'FINISHED', '2022-06-01 18:02:27', '2022-06-01 18:02:27');
INSERT INTO `flow_definition` VALUES ('3osi3b97pa4000', '延时器示例', '包含开始-延时器-输出等节点', 'test', 'FINISHED', '2022-05-09 15:16:18', '2022-05-09 15:16:18');
INSERT INTO `flow_definition` VALUES ('3uldr6qij5o000', '流程运行状态示例', '可查看流程运行状态，鼠标悬停状态图标，可查看详细信息', 'test', 'FINISHED', '2022-05-09 17:51:02', '2022-05-09 17:51:02');
INSERT INTO `flow_definition` VALUES ('3wccjaqq9y0000', '拼接节点示例', '使用拼接节点对分散的数据进行拼接', 'test', 'FINISHED', '2022-06-09 16:38:37', '2022-07-09 16:38:37');
INSERT INTO `flow_definition` VALUES ('41as9fmikt8000', '中止运行示例', '可在流程运行过程中，点击停止运行按钮，中止运行', 'test', 'FINISHED', '2022-05-09 17:47:20', '2022-05-09 17:47:20');
INSERT INTO `flow_definition` VALUES ('43bq2irvije000', '阀门节点示例', '可用于多条支路汇总时，限制某一时间周期内，通过此节点的执行次数', 'test', 'FINISHED', '2023-03-20 18:53:12', '2023-03-20 18:53:12');
INSERT INTO `flow_definition` VALUES ('48r380g6qc4000', '解析JSON示例', '包含开始-JSON 解析-输出等节点', 'test', 'FINISHED', '2022-05-10 15:11:38', '2022-10-31 16:28:21');
INSERT INTO `flow_definition` VALUES ('4d3es9ekt6m000', '分割节点示例', '使用分割节点对数据进行分割并输出', 'test', 'FINISHED', '2022-06-09 16:40:38', '2022-07-09 16:40:38');
INSERT INTO `flow_definition` VALUES ('4o7jgmb2n6g000', 'PostgreSQL节点使用示例', '使用PostgreSQL节点连接至指定的PostgreSQL数据库，并执行自定义sql语句，查看执行结果。前置条件：需要具备或已知PostgreSQL数据库地址，更新到节点URL属性上再运行流程', 'test', 'FINISHED', '2022-06-07 16:52:41', '2022-10-26 16:53:18');
INSERT INTO `flow_definition` VALUES ('4ufazyee92c000', 'Mysql节点使用示例', '使用Mysql节点连接至指定的mysql数据库，并执行自定义sql语句，查看执行结果。前置条件：需要具备或已知mysql数据库地址，更新到节点URL属性上再运行流程', 'test', 'FAILED', '2022-06-04 18:11:50', '2022-06-04 18:20:14');
INSERT INTO `flow_definition` VALUES ('4z9vdqsd5kg000', '增删改查-接口请求模型示例', '通过发送HTTP请求，对用户做增删改查操作的业务模型。前置条件：需要先运行流程[接口服务模型示例]', 'test', 'FINISHED', '2022-07-04 14:48:43', '2022-07-09 16:32:32');
INSERT INTO `flow_definition` VALUES ('5czjn0rra74000', '增删改查-接口服务模型示例', '展示了用户增删改查接口服务的业务模型。（先运行此流程后，再运行接口请求模型示例）', 'test', 'FINISHED', '2022-07-03 10:48:40', '2022-07-09 16:35:14');
INSERT INTO `flow_definition` VALUES ('5epeyaoy9ac000', '请求自定义HTTP服务', '发起HTTP请求，用于检查自定义HTTP服务的结果。前置条件：需要先运行流程[自定义HTTP服务示例]', 'test', 'FINISHED', '2022-06-19 15:13:06', '2022-06-19 15:13:06');
INSERT INTO `flow_definition` VALUES ('5pubb7joxmc000', '子流程示例-父流程', '用于演示嵌套流程的运行，本流程作为父流程', 'test', 'FINISHED', '2022-09-30 16:49:45', '2022-09-30 16:50:06');
INSERT INTO `flow_definition` VALUES ('6a088jl98eg00', '定时器示例', '仅包含定时器-输出两个节点', 'test', 'FINISHED', '2022-05-09 13:40:33', '2022-05-09 13:40:33');
INSERT INTO `flow_definition` VALUES ('ihvivrh5j1k00', '自定义HTTP服务示例', '创建自定义HTTP服务，处理请求并自定义响应数据', 'test', 'FINISHED', '2022-06-19 15:06:57', '2022-06-19 15:06:57');
INSERT INTO `flow_definition` VALUES ('m6o3aghqfrk00', '并行运行示例', '同一流程内可多条线路并行运行', 'test', 'FINISHED', '2022-05-09 17:58:27', '2022-05-09 17:58:27');
INSERT INTO `flow_definition` VALUES ('mk3eo1ewkk000', '节点参数传递', '任意节点之间都可以进行自定义参数传递，仅能从上一节点传递至下一节点，不可跨节点传递', 'test', 'FINISHED', '2022-05-09 15:20:00', '2022-05-09 15:56:06');
INSERT INTO `flow_definition` VALUES ('nm8hriar4ds00', '条件判断节点示例', '可使用条件节点组成与或等逻辑，可实现各种场景下的条件判断逻辑', 'test', 'FINISHED', '2022-07-05 10:05:57', '2022-07-05 15:08:32');
INSERT INTO `flow_definition` VALUES ('rjj5vbam81s00', '序列节点示例', '使用序列节点产生序列数并输出', 'test', 'FINISHED', '2022-06-09 16:37:05', '2022-07-09 16:37:05');
INSERT INTO `flow_definition` VALUES ('xu7fsb4whw000', '简单示例', '仅包含开始-输出两个节点', 'test', 'INIT', '2022-05-09 03:32:31', '2022-05-09 03:32:31');
