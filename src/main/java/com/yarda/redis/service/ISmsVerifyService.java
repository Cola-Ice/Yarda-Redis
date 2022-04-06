package com.yarda.redis.service;

/**
 * @author xuezheng
 * @date 2022/4/5-16:35
 */
public interface ISmsVerifyService {

    /**
     * 发送短信验证码
     * @param phoneNumber 手机号
     * @return 发送结果
     */
    public String sendSmsVerifyCode(String phoneNumber);

    /**
     * 核实验证码
     * @param phoneNumber 手机号
     * @param verifyCode 验证码
     * @return 校验结果
     */
    public String checkVerifyCode(String phoneNumber, String verifyCode);
}
