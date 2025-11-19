package com.flow.eda.server;

import com.flow.eda.server.runtime.node.http.HttpDispatcherServlet;
import com.flow.eda.server.common.ApplicationContextUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Flow EDA Server 统一服务入口
 * 合并了 flow-eda-web 和 flow-eda-runner 两个微服务
 */
@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
@EnableScheduling
@EnableCaching
@EnableTransactionManagement
@ComponentScan(
    basePackages = {
        "com.flow.eda.server",
        "com.flow.eda.common.config",
        "com.flow.eda.common.resource"
    }
)
@MapperScan("com.flow.eda.server")
public class FlowEdaServerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext =
                SpringApplication.run(FlowEdaServerApplication.class, args);
        ApplicationContextUtil.setApplicationContext(applicationContext);
    }

    @Bean
    @Qualifier(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
    public DispatcherServlet dispatcherServlet() {
        return new HttpDispatcherServlet();
    }
}
