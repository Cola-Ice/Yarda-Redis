package com.yarda.redis.service;

/**
 * @author xuezheng
 * @version 1.0
 * @date 2022/4/6 14:35
 */
public interface ILoginService {

    /**
     * 判断账户是否已限制登录
     * @param account 账号
     * @return true/false
     */
    boolean isLimitLogin(String account);

    /**
     * 校验账号/密码是否正确
     * @param account 账号
     * @param password 密码
     * @return true/false
     */
    boolean verifyAccountPassword(String account, String password);
}
