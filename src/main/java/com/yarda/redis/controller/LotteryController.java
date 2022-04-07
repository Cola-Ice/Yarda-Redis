package com.yarda.redis.controller;

import com.yarda.redis.service.ILotteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模拟抽奖-利用redis中set类型srandmember/spop
 * @author xuezheng
 * @version 1.0
 * @date 2022/4/7 18:36
 */
@RestController
@RequestMapping("/lottery")
public class LotteryController {

    @Autowired
    private ILotteryService lotteryService;

    /**
     * 随机抽取中奖幸运儿
     */
    @GetMapping("/person")
    public List<String> getLuckyPerson(Integer num){
        return lotteryService.getLuckyPerson(num);
    }

    /**
     * 添加候选人
     */
    @PostMapping("/candidate")
    public void addCandidate(String[] candidates){
        lotteryService.addCandidate(candidates);
    }
}
