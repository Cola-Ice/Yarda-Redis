package com.yarda.redis;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

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
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        // 模拟redis实现缓存功能
        String userInfo = valueOperations.get("user:Zzz");
        if(userInfo == null){
            // 获取数据库中用户信息，并存入redis
            userInfo = "username:李四,age:26,sex:1";
            valueOperations.set("user:Zzz", userInfo);
            System.out.println("从数据库中获取数据，并将其放入缓存");
        }
        System.out.println("userInfo>>>>>>>>>>" + userInfo);
    }

    /**
     * 测试 string类型:存储对象
     */
    @Test
    public void testStringObject(){
        ValueOperations<String,Map<String,String>> valueOperations = redisTemplate.opsForValue();
        // 模拟redis实现缓存功能
        Map<String,String> userInfo = valueOperations.get("user:wangwu");
        if(userInfo == null){
            // 获取数据库中用户信息，并存入redis
            userInfo = new HashMap<>(8);
            userInfo.put("username", "李四");
            userInfo.put("age", "26");
            userInfo.put("sex", "1");
            valueOperations.set("user:wangwu", userInfo);
            System.out.println("从数据库中获取数据，并将其放入缓存");
        }
        System.out.println("userInfo>>>>>>>>>>" + JSON.toJSONString(userInfo));
    }

    /**
     * 测试 hash类型
     */
    @Test
    public void testHash(){
        HashOperations<String,String,Object> hashOperations = redisTemplate.opsForHash();
        // 模拟redis实现缓存功能
        Map<String,Object> userInfo = hashOperations.entries("user:lisi");
        if(userInfo == null || userInfo.isEmpty()){
            // 获取数据库中用户信息，并存入redis
            userInfo = new HashMap<>(8);
            userInfo.put("username", "李四");
            userInfo.put("age", "26");
            userInfo.put("sex", "1");
            hashOperations.putAll("user:lisi", userInfo);
            System.out.println("从数据库中获取数据，并将其放入缓存");
        }
        System.out.println("userInfo>>>>>>>>>>" + JSON.toJSONString(userInfo));
    }

    /**
     * 测试 hash类型：hash中某个key操作
     */
    @Test
    public void testHashOps(){
        HashOperations<String,String,String> hashOperations = redisTemplate.opsForHash();
        // 新增、修改单个key
        hashOperations.put("user:lisi", "username", "王五");
        // 获取单个key对应的value
        String username = hashOperations.get("user:lisi", "username");
        System.out.println("username>>>>>>>>>>>>" + username);
    }

    /**
     * 测试 list类型
     */
    @Test
    public void testList(){
        ListOperations<String,String> listOperations = redisTemplate.opsForList();
        // 向列表中插入元素，右侧
        Long len = listOperations.rightPushAll("listthree", "1", "2", "3", "4", "5", "6", "7", "8");
        // 获取列表中指定索引对应的元素
        String element = listOperations.index("listthree", 0);
        System.out.println("list中下标为0的元素为>>>>>>>>>>>>" + element);
        // 从列表中弹出一个元素，左侧
        String popElement = listOperations.leftPop("listthree");
        System.out.println("list左侧弹出的元素为>>>>>>>>>>>>" + popElement);
    }
}
