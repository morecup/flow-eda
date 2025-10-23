package com.flow.eda.web.log;

import com.flow.eda.common.http.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LogContentProxyTest {

    private MockMvc mvc;
    private LogClient logClient;

    @BeforeEach
    void setup() {
        LogContentController controller = new LogContentController();
        logClient = Mockito.mock(LogClient.class);
        ReflectionTestUtils.setField(controller, "logClient", logClient);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void should_proxy_log_content() throws Exception {
        Mockito.when(logClient.getLogContent("/x")).thenReturn(Result.of("content"));
        mvc.perform(get("/api/v1/logs/content").param("path", "/x"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("content")));
    }
}
