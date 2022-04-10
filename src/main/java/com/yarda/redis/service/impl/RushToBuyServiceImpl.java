package com.yarda.redis.service.impl;

import com.alibaba.fastjson.JSON;
import com.yarda.redis.service.IRushToBuyService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xuezheng
 * @date 2022/4/9-21:23
 */
@Service
public class RushToBuyServiceImpl implements IRushToBuyService {

    @Resource
    private JedisPool jedisPool;

    /**
     * 秒杀
     * @param username 用户名
     * @param productId 产品id
     * @return 秒杀结果
     */
    @Override
    public String rushToBuy(String username, String productId) {
        Jedis jedis = jedisPool.getResource();
        // 参数校验
        if(!StringUtils.hasLength(username) || !StringUtils.hasLength(productId)){
            return "参数不正确";
        }
        String prodNumKey = "product:" + productId + ":num", userKey = "product:" + productId + ":user";
        // 监视库存
        jedis.watch(prodNumKey);
        // 查看是否有库存
        Object num = jedis.get(prodNumKey);
        if(num == null){
            return "秒杀尚未开始";
        }
        if(Integer.parseInt(num.toString()) <= 0){
            return "秒杀已结束";
        }
        // 查看用户是否重复秒杀
        if(jedis.sismember(userKey, username)){
            return "每个用户只可购买一次";
        }
        // 减少库存
        Transaction multi = jedis.multi();
        multi.decr(prodNumKey);
        multi.sadd(userKey, username);
        List list = multi.exec();
        jedis.close();
        System.out.println("执行结果：" + JSON.toJSONString(list));
        System.out.println("用户购买成功：" + username);
        return "success";
    }
}
