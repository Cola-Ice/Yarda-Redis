package com.yarda.redis.service;

import java.util.Set;

/**
 * @author xuezheng
 * @version 1.0
 * @date 2022/4/7 16:29
 */
public interface ICommonFriendsService {

    /**
     * 获取共同好友
     * @param username1 用户1
     * @param username2 用户2
     * @return 共同好友列表
     */
    Set<String> getCommonFriends(String username1, String username2);

    /**
     * 添加用户好友信息
     * @param username 用户
     * @param friendName 朋友
     * @return 添加结果
     */
    String addFriend(String username, String friendName);
}
