package com.yarda.redis.controller;

import com.yarda.redis.service.ICommonFriendsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Set;

/**
 * 模拟共同好友功能controller-利用redis中set类型的交集
 * @author xuezheng
 * @version 1.0
 * @date 2022/4/7 16:25
 */
@RestController
@RequestMapping("/common/friend")
public class CommonFriendsController {

    @Resource
    private ICommonFriendsService commonFriendsService;

    /**
     * 获取共同好友
     */
    @GetMapping
    public Set<String> getCommonFriends(String username1, String username2){
        return commonFriendsService.getCommonFriends(username1, username2);
    }

    /**
     * 添加用户好友信息
     */
    @PostMapping
    public String addFriend(String username, String friendName){
        return commonFriendsService.addFriend(username, friendName);
    }

}
