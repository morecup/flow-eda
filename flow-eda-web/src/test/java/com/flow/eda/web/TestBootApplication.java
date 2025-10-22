package com.flow.eda.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@EnableAutoConfiguration
@MapperScan("com.flow.eda.web.flow.instance")
@ComponentScan(basePackages = {
        "com.flow.eda.web.flow.instance"
})
public class TestBootApplication {
}


