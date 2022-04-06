package com.yarda.redis.controller;

import com.yarda.redis.service.ILoginService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 模拟登录限制-string:set,incr,expire
 * @author xuezheng
 * @version 1.0
 * @date 2022/4/6 14:28
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    @Resource
    private ILoginService loginService;
    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    /**
     * 登录：密码错误多次，进行登录限制
     */
    @PostMapping
    public String login(String account, String password){
        if(!StringUtils.hasLength(account) || !StringUtils.hasLength(password)){
            return "用户名/密码不能为空";
        }
        // 判断账号是否限制登录
        if(loginService.isLimitLogin(account)){
            return "账户已限制登录";
        }
        // 校验账号/密码
        boolean pass = loginService.verifyAccountPassword(account, password);
        if(pass){
            // 登录成功，失败次数清除
            redisTemplate.delete("login:fail:num:" + account);
            return "success";
        }else{
            // 登录失败次数记录
            redisTemplate.opsForValue().increment("login:fail:num:" + account);
            redisTemplate.expire("login:fail:num:" + account, 5, TimeUnit.MINUTES);
            return "用户名/密码不正确";
        }
    }

}
