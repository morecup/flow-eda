package com.flow.eda.server.runtime.node.generator.xml;

import com.flow.eda.common.exception.FlowException;
import org.bson.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class XmlGenerateNodeTest {

    @Test
    void generateXml_withOverrides() throws Exception {
        Document params = new Document();
        params.append("flowId", "flow-test");
        params.append("nodeId", "node-test");
        params.append("template", "test-template.xml");
        params.append(
                "overrides",
                new Document()
                        .append("Header.To", "Bob")
                        .append("Body.@id", "2")
                        .append("Body.Amount", 100)
                        .append("Body.Items.Item[1]", "BAR2"));

        XmlGenerateNode node = new XmlGenerateNode(params);

        final Document[] output = new Document[1];
        node.run((p) -> output[0] = p);

        assertNotNull(output[0]);
        String xml = output[0].getString("result");
        assertNotNull(xml);

        org.dom4j.Document doc = DocumentHelper.parseText(xml);
        Element root = doc.getRootElement();
        assertEquals("Root", root.getName());
        assertEquals("Bob", root.element("Header").elementText("To"));
        assertEquals("2", root.element("Body").attributeValue("id"));
        assertEquals("100", root.element("Body").elementText("Amount"));
        assertEquals(
                "BAR2",
                root.element("Body").element("Items").elements("Item").get(1).getTextTrim());
    }

    @Test
    void generateXml_withDynamicFilePath_shouldWriteFile(@TempDir Path tempDir) throws Exception {
        Path outFile = tempDir.resolve("a").resolve("b").resolve("out.xml");

        Document params = new Document();
        params.append("flowId", "flow-test");
        params.append("nodeId", "node-test");
        params.append("template", "test-template.xml");
        params.append("filePath", "${filePath}");
        params.append("input", new Document("filePath", outFile.toString()));
        params.append("overrides", new Document().append("Header.To", "Bob"));

        XmlGenerateNode node = new XmlGenerateNode(params);

        final Document[] output = new Document[1];
        node.run((p) -> output[0] = p);

        assertNotNull(output[0]);
        assertEquals(outFile.normalize().toString(), output[0].getString("writtenFilePath"));
        assertTrue(Files.exists(outFile));

        String fileContent = new String(Files.readAllBytes(outFile), StandardCharsets.UTF_8);
        org.dom4j.Document doc = DocumentHelper.parseText(fileContent);
        Element root = doc.getRootElement();
        assertEquals("Bob", root.element("Header").elementText("To"));
    }

    @Test
    void generateXml_invalidOverridePath_shouldThrow() {
        Document params = new Document();
        params.append("flowId", "flow-test");
        params.append("nodeId", "node-test");
        params.append("template", "test-template.xml");
        params.append("overrides", new Document().append("NotExist", "x"));

        FlowException e = assertThrows(FlowException.class, () -> new XmlGenerateNode(params));
        assertTrue(e.getMessage().contains("Cannot find element"));
    }

    @Test
    void generateXml_unresolvedFilePathPlaceholder_shouldThrow() {
        Document params = new Document();
        params.append("flowId", "flow-test");
        params.append("nodeId", "node-test");
        params.append("template", "test-template.xml");
        params.append("filePath", "${filePath}");

        FlowException e = assertThrows(FlowException.class, () -> new XmlGenerateNode(params));
        assertTrue(e.getMessage().contains("unresolved placeholder"));
    }
}
