package com.flow.eda.runner.node.http.request;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;

class HttpRequestNodeTest {

    @Test
    void run() {
        Document args = new Document();
        args.append("url", "http://localhost/test");
        args.append("method", "GET");
        args.append("header", "Accept:application/json");
        args.append("flowId", "f1");
        args.append("nodeId", "n1");

        CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);

        try (MockedStatic<HttpClients> mocked = Mockito.mockStatic(HttpClients.class)) {
            mocked.when(HttpClients::createDefault).thenReturn(httpClient);

            Mockito.when(httpClient.execute(Mockito.any(HttpRequestExpand.class))).thenReturn(response);
            Mockito.when(response.getEntity())
                    .thenReturn(new org.apache.http.entity.StringEntity("{\"result\":\"ok\"}", StandardCharsets.UTF_8));
            Header[] headers = new Header[] {new BasicHeader("Content-Type", "application/json")};
            Mockito.when(response.getHeaders("Content-Type")).thenReturn(headers);

            HttpRequestNode httpRequestNode = new HttpRequestNode(args);
            httpRequestNode.run(
                    p -> {
                        Assertions.assertTrue(p.containsKey("httpResult"));
                        Assertions.assertEquals("ok", ((Document) p.get("httpResult")).getString("result"));
                    });

            Mockito.verify(httpClient).execute(Mockito.any(HttpRequestExpand.class));
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }
}
