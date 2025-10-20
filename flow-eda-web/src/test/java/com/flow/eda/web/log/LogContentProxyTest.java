package com.flow.eda.web.log;

import com.flow.eda.common.http.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false"
})
public class LogContentProxyTest {

    @LocalServerPort
    int port;

    @MockBean
    private LogClient logClient;

    @Test
    void should_proxy_log_content() {
        Mockito.when(logClient.getLogContent("/x")).thenReturn(Result.of("content"));
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> r = rt.getForEntity("http://localhost:"+port+"/api/v1/logs/content?path=/x", String.class);
        Assertions.assertTrue(r.getStatusCode().is2xxSuccessful());
        Assertions.assertTrue(r.getBody().contains("content"));
    }
}
