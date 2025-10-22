package com.flow.eda.runner.node.http.response;

import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import com.flow.eda.runner.node.http.receive.HttpReceiveNode;

class HttpResponseNodeTest {

    @Test
    void run() {
        Document responseParams = new Document();
        responseParams.append("uri", "/api/v1/test");
        responseParams.append("method", "GET");
        responseParams.append("resData", "{\"result\": \"OK\"}");
        responseParams.append("flowId", "f1");
        responseParams.append("nodeId", "response");

        Document receiveParams = new Document();
        receiveParams.append("uri", "/api/v1/test");
        receiveParams.append("method", "GET");
        receiveParams.append("flowId", "f1");
        receiveParams.append("nodeId", "receive");

        HttpReceiveNode receiveNode = new HttpReceiveNode(receiveParams);
        receiveNode.run(p -> {});
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ReflectionTestUtils.setField(receiveNode, "response", servletResponse);

        HttpResponseNode httpResponseNode = new HttpResponseNode(responseParams);
        Document[] captured = new Document[1];
        httpResponseNode.run(p -> captured[0] = p);

        try {
            Document responseDoc = Document.parse(servletResponse.getContentAsString());
            Assertions.assertEquals("OK", responseDoc.getString("result"));
        } catch (java.io.UnsupportedEncodingException e) {
            Assertions.fail(e);
        }
        Assertions.assertNotNull(captured[0]);
        Assertions.assertEquals("OK", captured[0].getString("result"));

        receiveNode.destroy();
    }
}
