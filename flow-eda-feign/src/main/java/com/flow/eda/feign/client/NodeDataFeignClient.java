package com.flow.eda.feign.client;

import com.flow.eda.feign.http.Result;
import com.flow.eda.feign.dto.NodeDataRunRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** 节点运行 Feign 调用 */
@FeignClient(name = "flow-eda-server", contextId = "nodeDataFeignClient", path = "/api/v1")
public interface NodeDataFeignClient {

    @PostMapping("/node/data/run")
    Result<String> runNodeData(@RequestBody NodeDataRunRequest request);
}
