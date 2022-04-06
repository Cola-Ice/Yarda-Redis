package com.yarda.redis.service.impl;

import com.yarda.redis.service.ISmsVerifyService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码 service 实现
 * @author xuezheng
 * @date 2022/4/5-16:35
 */
@Service
public class SmsVerifyServiceImpl implements ISmsVerifyService {

    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    /**
     * 发送短信验证码
     * @param phoneNumber 手机号
     */
    @Override
    public String sendSmsVerifyCode(String phoneNumber) {
        // 1.判断该手机号验证码发送已超限
        Integer frequency = (Integer)redisTemplate.opsForValue().get("verifyCode:" + phoneNumber + ":frequency");
        if(frequency != null && frequency >= 3){
            return "该手机号当天验证码发送次数已超限制";
        }
        redisTemplate.opsForValue().increment("verifyCode:" + phoneNumber + ":frequency");
        redisTemplate.expire("verifyCode:" + phoneNumber + ":frequency", 1, TimeUnit.HOURS);
        // 2.发送验证码,仅仅作演示
        String verifyCode = "0000";
        System.out.println("发送验证码>>>>>>>>>>" + verifyCode);
        // 3.保存验证码信息，有效期5分钟
        redisTemplate.opsForValue().set("verifyCode:" + phoneNumber + ":latestCode", verifyCode, 5, TimeUnit.MINUTES);
        return "success";
    }

    /**
     * 核实验证码
     * @param phoneNumber 手机号
     * @param verifyCode 验证码
     * @return 校验结果
     */
    @Override
    public String checkVerifyCode(String phoneNumber, String verifyCode) {
        // 1.获取最新发送的验证码
        String latestCode = (String)redisTemplate.opsForValue().get("verifyCode:" + phoneNumber + ":latestCode");
        // 2.校验验证码
        if(latestCode == null || !latestCode.equals(verifyCode)){
            return "验证码不正确或已失效";
        }
        // 3.删除验证码
        redisTemplate.delete("verifyCode:" + phoneNumber + ":latestCode");
        return "success";
    }


}
