package com.yarda.redis.service.impl;

import com.yarda.redis.service.ILoginService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 登录限制模拟 service 实现
 * @author xuezheng
 * @version 1.0
 * @date 2022/4/6 14:35
 */
@Service
public class LoginServiceImpl implements ILoginService {

    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    /**
     * 判断账户是否已限制登录
     * @param account 账号
     * @return true/false
     */
    @Override
    public boolean isLimitLogin(String account) {
        Integer num = (Integer)redisTemplate.opsForValue().get("login:fail:num:" + account);
        if(num != null && num >= 5){
            return true;
        }
        return false;
    }

    /**
     * 校验账号/密码是否正确
     * @param account 账号
     * @param password 密码
     * @return true/false
     */
    @Override
    public boolean verifyAccountPassword(String account, String password) {
        if("admin".equals(account) && "123456".equals(password)){
            return true;
        }
        return false;
    }
}
