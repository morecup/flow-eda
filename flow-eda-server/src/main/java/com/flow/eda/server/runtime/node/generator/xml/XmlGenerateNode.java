package com.flow.eda.server.runtime.node.generator.xml;

import com.flow.eda.common.exception.FlowException;
import com.flow.eda.server.runtime.node.AbstractNode;
import com.flow.eda.server.runtime.node.NodeFunction;
import com.flow.eda.server.runtime.node.NodeVerify;
import com.flow.eda.server.utils.XmlTemplateUtil;
import org.bson.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** XML 生成节点：基于 XML 模板生成新的 XML，并支持参数覆盖 */
public class XmlGenerateNode extends AbstractNode {
    private String xml;
    private String filePath;

    public XmlGenerateNode(Document params) {
        super(params);
    }

    @Override
    public void run(NodeFunction callback) {
        // 写入文件（可选）
        if (StringUtils.hasText(this.filePath)) {
            writeFile(this.filePath, this.xml);
        }

        // 始终透传上游 input，保证下游节点可继续使用占位符取值
        Document out = output();
        Document passthroughInput = new Document();
        passthroughInput.putAll(getInput());
        Document payloadInput = out.get("input", Document.class);
        if (payloadInput != null && !payloadInput.isEmpty()) {
            passthroughInput.putAll(payloadInput);
        }
        out.put("input", passthroughInput);

        out.append("result", xml);
        if (StringUtils.hasText(this.filePath)) {
            // 避免与节点入参 filePath 冲突，使用 writtenFilePath 输出
            out.append("writtenFilePath", this.filePath);
        }

        setStatus(Status.FINISHED);
        callback.callback(out);
    }

    @Override
    protected void verify(Document params) {
        String template = params.getString("template");
        NodeVerify.notBlank(template, "template");

        String templateContent = XmlTemplateUtil.loadTemplate(template);
        NodeVerify.notBlank(templateContent, "template");

        org.dom4j.Document doc;
        try {
            doc = DocumentHelper.parseText(templateContent);
        } catch (Exception e) {
            throw new FlowException("Incorrect the xml template format");
        }

        Document overrides = parseOverrides(params.get("overrides"));
        applyOverrides(doc.getRootElement(), overrides);

        this.xml = doc.asXML();

        // 可选：写入文件路径（支持占位符替换后的动态值）
        String filePath = params.getString("filePath");
        if (StringUtils.hasText(filePath)) {
            filePath = filePath.trim();
            // 如果占位符未被解析，直接报错，避免写入到奇怪路径
            if (filePath.contains("${")) {
                throw new FlowException("The filePath contains unresolved placeholder: " + filePath);
            }
            this.filePath = filePath;
        }
    }

    private void writeFile(String filePath, String content) {
        try {
            Path path = Paths.get(filePath).normalize();
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(
                    path,
                    content.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            // 归一化后的路径写回输出
            this.filePath = path.toString();
        } catch (IOException e) {
            throw FlowException.wrap(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Document parseOverrides(Object raw) {
        if (raw == null) {
            return new Document();
        }
        if (raw instanceof Document) {
            return (Document) raw;
        }
        if (raw instanceof Map) {
            return new Document((Map<String, Object>) raw);
        }
        if (raw instanceof String) {
            String s = ((String) raw).trim();
            if (!StringUtils.hasText(s)) {
                return new Document();
            }
            try {
                return Document.parse(s);
            } catch (Exception e) {
                NodeVerify.throwWithName("overrides");
            }
        }
        NodeVerify.throwWithName("overrides");
        return new Document();
    }

    private void applyOverrides(Element root, Document overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : overrides.entrySet()) {
            applyOverride(root, entry.getKey(), entry.getValue());
        }
    }

    private void applyOverride(Element root, String path, Object value) {
        if (!StringUtils.hasText(path)) {
            NodeVerify.throwWithName("overrides");
        }
        String v = valueToString(value);

        List<String> segments = splitPath(path.trim());
        if (segments.isEmpty()) {
            throw new FlowException("The xml override path is blank");
        }

        // 允许传入包含根节点名称的完整路径：Root.Header.To
        if (segments.size() > 1 && root.getName().equals(segments.get(0))) {
            segments = segments.subList(1, segments.size());
        }

        Element current = root;
        for (int i = 0; i < segments.size(); i++) {
            String segment = segments.get(i);
            boolean last = i == segments.size() - 1;

            // 属性覆盖：Header.@id 或 Header/@id
            if (segment.startsWith("@")) {
                if (!last) {
                    throw new FlowException("The attribute segment must be the last one: " + path);
                }
                String attr = segment.substring(1);
                if (!StringUtils.hasText(attr)) {
                    throw new FlowException("The attribute name is blank: " + path);
                }
                current.addAttribute(attr, v);
                return;
            }

            NameIndex nameIndex = parseNameIndex(segment, path);
            List<Element> children = current.elements(nameIndex.name);
            if (children == null || children.isEmpty()) {
                throw new FlowException(
                        "Cannot find element '" + nameIndex.name + "' in path: " + path);
            }
            int idx = nameIndex.index == null ? 0 : nameIndex.index;
            if (idx < 0 || idx >= children.size()) {
                throw new FlowException("The index is out of range in path: " + path);
            }
            Element next = children.get(idx);

            if (last) {
                next.setText(v);
                return;
            }
            current = next;
        }
    }

    private static class NameIndex {
        private final String name;
        private final Integer index;

        private NameIndex(String name, Integer index) {
            this.name = name;
            this.index = index;
        }
    }

    private NameIndex parseNameIndex(String segment, String path) {
        String s = segment == null ? null : segment.trim();
        if (!StringUtils.hasText(s)) {
            throw new FlowException("The xml override path is invalid: " + path);
        }

        // 支持：Item[0]
        if (s.endsWith("]")) {
            int i = s.lastIndexOf('[');
            if (i > 0) {
                String name = s.substring(0, i);
                String indexStr = s.substring(i + 1, s.length() - 1);
                if (!StringUtils.hasText(name) || !StringUtils.hasText(indexStr)) {
                    throw new FlowException("The xml override path is invalid: " + path);
                }
                try {
                    return new NameIndex(name, Integer.parseInt(indexStr));
                } catch (NumberFormatException e) {
                    throw new FlowException("The xml override index must be an integer: " + path);
                }
            }
        }

        return new NameIndex(s, null);
    }

    private List<String> splitPath(String path) {
        List<String> segments = new ArrayList<>();

        // 支持 /Root/Header/To 形式
        if (path.contains("/")) {
            for (String s : path.split("/")) {
                if (StringUtils.hasText(s)) {
                    segments.add(s);
                }
            }
            return segments;
        }

        // 默认使用 . 分隔：Header.To
        for (String s : path.split("\\.")) {
            if (StringUtils.hasText(s)) {
                segments.add(s);
            }
        }
        return segments;
    }

    private String valueToString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Document) {
            return ((Document) value).toJson();
        }
        return String.valueOf(value);
    }
}
