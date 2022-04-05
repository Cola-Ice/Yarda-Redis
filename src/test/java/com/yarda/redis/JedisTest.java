package com.yarda.redis;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class JedisTest {

    @Autowired
    private JedisPool jedisPool;

    /**
     * 测试通过jedis客户端连接redis服务器
     */
    @Test
    void testJedis() {
        // 获取连接对象
        Jedis jedis = jedisPool.getResource();
        jedis.set("jedis", "jedisValue");
        String result = jedis.get("jedis");
        System.out.println("result>>>>>>>>>>>>>>>>" + result);
        // 关闭连接
        jedis.close();
    }

    /**
     * jedis 操作redis的string类型
     */
    @Test
    void testString(){
        Jedis jedis = jedisPool.getResource();
        // 模拟redis实现缓存功能
        String userInfo = jedis.get("user:kele");
        if(userInfo == null){
            // 获取数据库中用户信息，并存入redis
            userInfo = "username:zhangsan,age:26,sex:1";
            jedis.set("user:kele", userInfo);
            System.out.println("从数据库中获取数据，并将其放入缓存");
        }
        System.out.println("userInfo>>>>>>>>>>" + userInfo);
        jedis.close();
    }

    /**
     * jedis 操作redis的hash类型
     */
    @Test
    void testHash(){
        Jedis jedis = jedisPool.getResource();
        Map<String, String> userInfo = jedis.hgetAll("user:zhangsan");
        if(userInfo == null || userInfo.isEmpty()){
            // 获取数据库中用户信息，并存入redis
            userInfo = new HashMap<>(8);
            userInfo.put("username", "zhangsan");
            userInfo.put("age", "26");
            userInfo.put("sex", "1");
            jedis.hset("user:zhangsan", userInfo);
            System.out.println("从数据库中获取数据，并将其放入缓存");
        }
        System.out.println("userInfo>>>>>>>>>>" + JSON.toJSONString(userInfo));
        jedis.close();
    }

}
