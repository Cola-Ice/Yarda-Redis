package com.yarda.redis.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Redis属性类 Properties
 * @author xuezheng
 * @date 2022/4/3-20:17
 */
@Data
@Configuration
@ConfigurationProperties("spring.redis")
public class RedisProperties {
    private String host;
    private Integer port;
    private Integer database;
    private String password;
    @Value("${spring.redis.jedis.pool.max-active}")
    private Integer maxActive;
    @Value("${spring.redis.jedis.pool.max-idle}")
    private Integer maxIdle;
    @Value("${spring.redis.jedis.pool.min-idle}")
    private Integer minIdle;
}
