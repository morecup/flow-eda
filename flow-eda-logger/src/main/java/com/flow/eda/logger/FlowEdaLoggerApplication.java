package com.flow.eda.logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(
        basePackages = {
                "com.flow.eda.logger",
                "com.flow.eda.common.config",
                "com.flow.eda.common.resource"
        })
public class FlowEdaLoggerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowEdaLoggerApplication.class, args);
    }

    // WebSocket 已移除，无需 ServerEndpointExporter
}
