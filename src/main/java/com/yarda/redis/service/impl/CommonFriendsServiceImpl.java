package com.yarda.redis.service.impl;

import com.yarda.redis.service.ICommonFriendsService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author xuezheng
 * @version 1.0
 * @date 2022/4/7 16:33
 */
@Service
public class CommonFriendsServiceImpl implements ICommonFriendsService {

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 获取共同好友
     * @param username1 用户1
     * @param username2 用户2
     * @return 共同好友列表
     */
    @Override
    public Set<String> getCommonFriends(String username1, String username2) {
        return redisTemplate.opsForSet().intersect("userFriend:" + username1, "userFriend:" + username2);
    }

    /**
     * 添加用户好友信息
     * @param username 用户
     * @param friendName 朋友
     * @return 添加结果
     */
    @Override
    public String addFriend(String username, String friendName) {
        Long num = redisTemplate.opsForSet().add("userFriend:" + username, friendName);
        return (num == null || num == 0) ?  "该好友已存在" : "success";
    }
}
