package com.yarda.redis.controller;

import org.springframework.web.bind.annotation.*;

/**
 * 短信验证码 controller
 * @author xuezheng
 * @date 2022/4/5-16:19
 */
@RestController
@RequestMapping("/sms/code")
public class SmsVerifyCodeController {

    /**
     * 获取短信验证码
     */
    @GetMapping("/{phoneNumber}")
    public void getSmsVerifyCode(@PathVariable("phoneNumber") String phoneNumber){
        // 1.判断该手机号验证码发送已超限

        // 2.发送验证码

        // 3.保存验证码信息
    }

    /**
     * 校验短信验证码
     */
    @PostMapping("/verify")
    public String getSmsVerifyCode(String phoneNumber, String verifyCode){
        return null;
    }
}
