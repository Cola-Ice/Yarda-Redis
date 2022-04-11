package com.yarda.redis.service.impl;

import com.alibaba.fastjson.JSON;
import com.yarda.redis.service.IRushToBuyService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xuezheng
 * @date 2022/4/9-21:23
 */
@Service
public class RushToBuyServiceImpl implements IRushToBuyService {

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 秒杀
     * @param username 用户名
     * @param productId 产品id
     * @return 秒杀结果
     */
    @Override
    public String rushToBuy(String username, String productId) {
        // 参数校验
        if(!StringUtils.hasLength(username) || !StringUtils.hasLength(productId)){
            return "参数不正确";
        }
        String prodNumKey = "product:" + productId + ":num", userKey = "product:" + productId + ":user";
        // 由于redisTemplate默认每次执行重新拿一个连接，所以我们这里使用sessionCallback将所有事务命令放在同一个session中进行
        SessionCallback<String> sessionCallback = new SessionCallback<String>() {
            @Override
            public String execute(RedisOperations operations) throws DataAccessException {
                // 监视库存
                operations.watch(prodNumKey);
                // 查看是否有库存
                Object num = operations.opsForValue().get(prodNumKey);
                if(num == null){
                    return "秒杀尚未开始";
                }
                if(Integer.parseInt(num.toString()) <= 0){
                    return "秒杀已结束";
                }
                // 查看用户是否重复秒杀
                if(operations.opsForSet().isMember(userKey, username)){
                    return "每个用户只可购买一次";
                }
                // 减少库存
                operations.multi();
                operations.opsForValue().decrement(prodNumKey);
                operations.opsForSet().add(userKey, username);
                List list = operations.exec();
                if(list.isEmpty()){
                    return "系统繁忙，请稍后再试";
                }
                System.out.println("执行结果：" + JSON.toJSONString(list));
                System.out.println("用户购买成功：" + username);
                return "success";
            }
        };
        return redisTemplate.execute(sessionCallback);
    }
}
