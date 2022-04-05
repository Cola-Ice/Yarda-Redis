package com.yarda.redis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;

/**
 * @author xuezheng
 * @date 2022/4/5-17:36
 */
@SpringBootTest
public class RedisTemplateTest {

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 测试 string类型
     */
    @Test
    public void testString(){
        // 模拟redis实现缓存功能
        Object userInfo = redisTemplate.opsForValue().get("user:Zzz");
        if(userInfo == null){
            // 获取数据库中用户信息，并存入redis
            userInfo = "username:李四,age:26,sex:1";
            redisTemplate.opsForValue().set("user:Zzz", userInfo);
            System.out.println("从数据库中获取数据，并将其放入缓存");
        }
        System.out.println("userInfo>>>>>>>>>>" + userInfo);
    }
}
