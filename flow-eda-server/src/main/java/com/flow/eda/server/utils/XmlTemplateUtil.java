package com.flow.eda.server.utils;

import com.flow.eda.common.exception.FlowException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** XML 模板读取工具 */
public class XmlTemplateUtil {
    private static final String DEFAULT_CLASSPATH_DIR = "xml-template/";

    private XmlTemplateUtil() {}

    /**
     * 加载 XML 模板内容
     *
     * <p>支持三种写法：</p>
     *
     * <ul>
     *   <li>classpath:xml-template/a.xml</li>
     *   <li>file:D:/path/a.xml</li>
     *   <li>a.xml（默认从 classpath:xml-template/ 下读取）</li>
     * </ul>
     */
    public static String loadTemplate(String template) {
        if (!StringUtils.hasText(template)) {
            throw new FlowException("The xml template is blank");
        }
        String t = template.trim();

        // 显式 classpath
        if (t.startsWith("classpath:")) {
            String path = t.substring("classpath:".length());
            while (path.startsWith("/")) {
                path = path.substring(1);
            }
            return readClasspath(path);
        }

        // 显式文件
        if (t.startsWith("file:")) {
            String path = t.substring("file:".length());
            return readFile(Paths.get(path));
        }

        // 绝对路径/存在的相对路径
        Path path = Paths.get(t);
        if (path.isAbsolute() || Files.exists(path)) {
            return readFile(path);
        }

        // 默认从 classpath:xml-template/ 下读取
        return readClasspath(DEFAULT_CLASSPATH_DIR + t);
    }

    private static String readClasspath(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                throw new FlowException("Cannot find the xml template: " + path);
            }
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw FlowException.wrap(e);
        }
    }

    private static String readFile(Path path) {
        try {
            if (!Files.exists(path)) {
                throw new FlowException("Cannot find the xml template file: " + path);
            }
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw FlowException.wrap(e);
        }
    }
}
