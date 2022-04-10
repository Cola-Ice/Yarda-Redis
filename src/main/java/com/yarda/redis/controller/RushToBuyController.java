package com.yarda.redis.controller;

import com.yarda.redis.service.IRushToBuyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模拟秒杀-利用redis的watch机制实现的乐观锁来解决事务冲突
 * @author xuezheng
 * @date 2022/4/9-21:16
 */
@RestController
@RequestMapping("/rushBuy")
public class RushToBuyController {

    @Autowired
    private IRushToBuyService rushToBuyService;

    /**
     * 秒杀
     */
    @PostMapping
    public String rushToBuy(String username,String productId){
        return rushToBuyService.rushToBuy(username, productId);
    }
}
