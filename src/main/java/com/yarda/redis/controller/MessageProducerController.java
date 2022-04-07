package com.yarda.redis.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 消息生产者 controller：利用redis中rpush模拟消息生产者
 * @author xuezheng
 * @version 1.0
 * @date 2022/4/7 10:46
 */
@RestController
@RequestMapping("/message/producer")
public class MessageProducerController {

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 生产者发送消息
     */
    @PostMapping
    public String sendMessage(String message){
        if(!StringUtils.hasLength(message)){
            return "消息不能为空";
        }
        redisTemplate.opsForList().rightPush("message:queue", message);
        return "success";
    }
}
