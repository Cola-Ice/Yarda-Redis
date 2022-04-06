package com.yarda.redis.controller;

import com.yarda.redis.service.ISmsVerifyService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 短信验证码 controller
 * @author xuezheng
 * @date 2022/4/5-16:19
 */
@RestController
@RequestMapping("/sms/verifyCode")
public class SmsVerifyCodeController {

    @Resource
    private ISmsVerifyService smsVerifyService;

    /**
     * 获取短信验证码
     */
    @GetMapping("/{phoneNumber}")
    public String sendSmsVerifyCode(@PathVariable("phoneNumber") String phoneNumber){
        return smsVerifyService.sendSmsVerifyCode(phoneNumber);
    }

    /**
     * 校验短信验证码
     */
    @PostMapping("/verify")
    public String checkVerifyCode(String phoneNumber, String verifyCode){
        return smsVerifyService.checkVerifyCode(phoneNumber, verifyCode);
    }
}
