package com.flow.eda.web.flow.instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FlowInstanceControllerTest {

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        FlowInstanceService service = Mockito.mock(FlowInstanceService.class);
        Mockito.when(service.startInstance(Mockito.anyString(), Mockito.anyString())).thenReturn("inst-1");
        Mockito.when(service.getInstance("inst-1")).thenReturn(mockInstance("inst-1"));
        mvc = MockMvcBuilders.standaloneSetup(new FlowInstanceController(service)).build();
    }

    @Test
    void should_start_instance_and_return_id() throws Exception {
        String body = "{\"flowId\":\"f1\",\"triggerUser\":\"tester\"}";
        mvc.perform(post("/api/flow/instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.instanceId").isNotEmpty());
    }

    @Test
    void should_get_instance_detail() throws Exception {
        mvc.perform(get("/api/flow/instances/{id}", "inst-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instanceId").value("inst-1"))
                .andExpect(jsonPath("$.status").isNotEmpty());
    }

    private FlowInstanceDO mockInstance(String id) {
        FlowInstanceDO d = new FlowInstanceDO();
        d.setId(id);
        d.setStatus("RUNNING");
        return d;
    }
}


