package com.flow.eda.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 白名单配置
 *
 * @author flow-eda
 */
@Data
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "ignore")
public class IgnoreWhiteProperties {

    /**
     * 白名单路径列表
     */
    private List<String> whites = new ArrayList<>();
}
