package com.yarda.redis.service;

/**
 * @author xuezheng
 * @date 2022/4/9-21:22
 */
public interface IRushToBuyService {

    /**
     * 秒杀
     * @param username 用户名
     * @param productId 产品id
     * @return 秒杀结果
     */
    String rushToBuy(String username, String productId);
}
