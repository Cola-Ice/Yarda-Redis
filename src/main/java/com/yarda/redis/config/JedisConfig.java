package com.yarda.redis.config;

import com.yarda.redis.config.properties.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Jedis 配置类(手动整合jedis)
 * @author xuezheng
 * @date 2022/4/3-20:16
 */
//@Configuration
public class JedisConfig {

    /**
     * 配置jedis连接池
     * @return
     */
    @Bean
    public JedisPool jedisPool(RedisProperties properties){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(properties.getMaxActive());
        config.setMaxIdle(properties.getMaxIdle());
        config.setMinIdle(properties.getMinIdle());
        return new JedisPool(config, properties.getHost(), properties.getPort(), 2000, properties.getPassword(), properties.getDatabase());
    }
}
