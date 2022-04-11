package com.yarda.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * spring boot redisTemplate 配置
 * @author xuezheng
 * @date 2022/4/5-17:25
 */
@Configuration
public class RedisConfig {

    /**
     * 配置redisTemplate
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        // 默认序列化是JDK，导致redis中数据不够直观
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(mapper);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key 序列化方式 stringRedisSerializer
        template.setKeySerializer(stringRedisSerializer);
        // value 序列化方式 jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash key 序列化方式 stringRedisSerializer
        template.setHashKeySerializer(stringRedisSerializer);
        // hash value 序列化方式 jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        // 开启redisTemplate对事务的支持
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }
}
