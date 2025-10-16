package com.flow.eda.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关启动程序
 *
 * @author flow-eda
 */
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class FlowEdaGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowEdaGatewayApplication.class, args);
        System.out.println("Flow-EDA 网关启动成功");
    }
}
