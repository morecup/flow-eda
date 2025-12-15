# XML 模板目录

把需要复用的 XML 模板文件放在此目录下，运行时可在 [XML生成] 节点中通过 `template` 参数直接填写文件名加载，例如：

- `template = example.xml`  => 读取 `classpath:xml-template/example.xml`

也可以使用：

- `template = classpath:xml-template/example.xml`
- `template = file:D:/path/to/example.xml`
