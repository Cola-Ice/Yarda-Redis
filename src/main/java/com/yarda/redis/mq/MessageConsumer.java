package com.yarda.redis.mq;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 消息消费者-利用redis中blpop命令实现消息阻塞消费
 * @author xuezheng
 * @version 1.0
 * @date 2022/4/7 10:49
 */
@Component
public class MessageConsumer implements InitializingBean {

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 消费方法
     */
    public void consumer(){
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        while (true) {
            String message = null;
            try {
                // 拉取消息，没有消息时，阻塞等待1s
                message = listOperations.leftPop("message:queue", 1, TimeUnit.SECONDS);
                if(message != null){
                    // 消费消息
                    System.out.println("收到消息>>>>>>>>>>>>>>>" + message);
                }else{
//                    System.out.println("未拉取到消息，等待中>>>>>>>>>>>");
                }
            } catch (Exception e) {
                System.out.println("消费消息时出现异常>>>>>>>>>>>>" + message + ";异常原因：" + e.getMessage());
                // 消息重新退回队列
                if (message != null) {
                    listOperations.leftPush("message:queue", message);
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread consumer = new Thread(this::consumer);
        consumer.start();
    }
}
