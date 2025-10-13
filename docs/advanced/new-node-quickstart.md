# 新增节点快速清单（Checklist）

面向在本项目中快速新增一个“功能节点”的最小步骤说明，结合已有实现（如 HttpRequestNode、TifPathNode、StandardizeParamNode）。

## 1. Runner 实现（flow-eda-runner）

1) 新建节点类
- 位置：`com.flow.eda.runner.node.<your_feature>` 下新建 `XxxNode.java`
- 必须：`public XxxNode(Document params)` 构造；继承 `AbstractNode`
- 在 `verify(Document params)` 中：
  - 使用 `NodeVerify.*` 校验并解析参数
  - 如需从上游取值，使用 `getInput()`（占位符在父类已替换）
- 在 `run(NodeFunction fn)` 中：
  - 完成业务逻辑
  - `setStatus(Status.FINISHED)`
  - 用 `fn.callback(output().append("key", value))` 输出给下游

2) 注册节点类型
- 编辑 `com.flow.eda.runner.node.NodeTypeEnum`
- 增加枚举项：`YOUR_TYPE("your_type", XxxNode.class)`，其中 `your_type` 为前后端统一使用的类型字符串

3) 构建校验
- 执行：`mvn -q -pl flow-eda-runner -am -DskipTests package`

> 建议：按 HttpRequestNode 的风格组织 verify/run，不依赖 NodeParamsUtil，直接通过 `output().append(...)` 组装输出。

## 2. Web/DB 配置（flow-eda-web + MySQL）

1) 节点分组（菜单）
- 若使用现有分组：`基础/运算/算法/解析/网络/数据库/子流程`，无需改代码
- 若新增分组：在 `NodeTypeService.TYPE_MENU` 中追加分组名称以控制前端分组与顺序

2) 数据库注册节点类型（前端渲染来源）
- 表：`eda_flow_node_type`（核心字段：`type`、`type_name`、`menu`、`svg`、`background`、`description`）
- 表：`eda_flow_node_type_param`（定义参数表单：`type_id`、`key`、`name`、`required`、`in_type`、`option`、`placeholder`）
- 推荐：新增独立种子脚本（例：`flow-eda-common/sql/seed_xxx_node.sql`）便于重复部署

3) 缓存刷新
- `NodeTypeService.getNodeTypes` 使用缓存，变更后需重启 `flow-eda-web` 或清理缓存

## 3. 前端展示

- 前端会根据 `eda_flow_node_type` 自动显示节点，并按 `menu` 分组
- 图标：`svg` 指向前端 `public/svg/*.svg`，必要时新增 SVG 文件
- 背景色：`background` 直接配置为 CSS 颜色串（如 `rgb(200 180 75 / 60%)`）

## 4. 验证与测试

- 画一个最小流程：`开始 -> 新节点 -> 输出`，运行并观察输出
- 单元测试（可选）：为复杂节点在 runner 模块新增测试类，构造 `input` 与 `params` 做断言

## 5. 常用 SQL 模板（示例）

```sql
-- 节点类型
INSERT INTO eda_flow_node_type (id, type, type_name, menu, svg, background, description)
SELECT (SELECT COALESCE(MAX(id),0)+1 FROM eda_flow_node_type),
       'your_type','你的节点名','算法','/svg/sequence.svg','rgb(200 180 75 / 60%)','你的描述'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM eda_flow_node_type WHERE type='your_type');

-- 参数定义（可选）
INSERT INTO eda_flow_node_type_param (type_id, `key`, `name`, required, in_type, `option`, placeholder)
SELECT t.id, 'yourParam', '参数名', 0, 'input', NULL, '提示占位'
FROM eda_flow_node_type t
WHERE t.type='your_type' AND NOT EXISTS (
  SELECT 1 FROM eda_flow_node_type_param p WHERE p.type_id=t.id AND p.`key`='yourParam'
);
```

### 5.1 常用 MySQL 命令（bash）

- 执行 SQL 脚本：

```bash
mysql -h <HOST> -P <PORT> -u <USER> -p'<PASS>' -D flow_eda < /path/to/seed_xxx_node.sql
```

- 或使用 source 执行脚本：

```bash
mysql -h <HOST> -P <PORT> -u <USER> -p'<PASS>' -D flow_eda -e "source /path/to/seed_xxx_node.sql"
```

- 直接在线执行多条 SQL（示例：注册 tif_path 节点并设置到“算法”分组）：

```bash
mysql -h <HOST> -P <PORT> -u <USER> -p'<PASS>' -D flow_eda -e "\
INSERT INTO eda_flow_node_type (id,type,type_name,menu,svg,background,description) \
SELECT (SELECT COALESCE(MAX(id),0)+1 FROM eda_flow_node_type),'tif_path','TIF路径','算法','/svg/sequence.svg','rgb(200 180 75 / 60%)','根据输入的 inputTifPath，输出父级目录到 outputTifPath' \
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM eda_flow_node_type WHERE type='tif_path'); \
INSERT INTO eda_flow_node_type_param (type_id,`key`,`name`,required,in_type,`option`,placeholder) \
SELECT t.id,'inputTifPath','TIF路径',0,'input',NULL,'D:/path/to/file.tif' FROM eda_flow_node_type t \
WHERE t.type='tif_path' AND NOT EXISTS (SELECT 1 FROM eda_flow_node_type_param p WHERE p.type_id=t.id AND p.`key`='inputTifPath');"
```

- 修改分组（菜单）示例：

```bash
mysql -h <HOST> -P <PORT> -u <USER> -p'<PASS>' -D flow_eda -e "UPDATE eda_flow_node_type SET menu='算法' WHERE type='tif_path';"
```

- 校验插入是否成功：

```bash
mysql -h <HOST> -P <PORT> -u <USER> -p'<PASS>' -D flow_eda -e "SELECT id,type,menu FROM eda_flow_node_type WHERE type='tif_path';"
mysql -h <HOST> -P <PORT> -u <USER> -p'<PASS>' -D flow_eda -e "SELECT `key`,`name` FROM eda_flow_node_type_param WHERE type_id=(SELECT id FROM eda_flow_node_type WHERE type='tif_path');"
```

- 回滚/删除该节点：

```bash
mysql -h <HOST> -P <PORT> -u <USER> -p'<PASS>' -D flow_eda -e "\
SET @id := (SELECT id FROM eda_flow_node_type WHERE type='your_type'); \
DELETE FROM eda_flow_node_type_param WHERE type_id=@id; \
DELETE FROM eda_flow_node_type WHERE id=@id;"
```

## 6. 最小代码模板

```java
public class YourNode extends AbstractNode {
    // private String yourParam; // 如需参数

    public YourNode(Document params) { super(params); }

    @Override
    public void run(NodeFunction fn) {
        // 业务逻辑 ...
        setStatus(Status.FINISHED);
        fn.callback(output().append("result", "ok"));
    }

    @Override
    protected void verify(Document params) {
        // yourParam = params.getString("yourParam");
        // NodeVerify.notBlank(yourParam, "yourParam");
    }
}
```

完成以上步骤后，即可在编辑器左侧节点列表中看到新节点，拖拽配置并运行验证。
