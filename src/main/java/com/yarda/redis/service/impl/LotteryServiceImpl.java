package com.yarda.redis.service.impl;

import com.yarda.redis.service.ILotteryService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xuezheng
 * @version 1.0
 * @date 2022/4/7 18:44
 */
@Service
public class LotteryServiceImpl implements ILotteryService {

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 随机抽取获奖幸运儿
     * @return 幸运儿列表
     * @param num 抽取数量
     */
    @Override
    public List<String> getLuckyPerson(Integer num) {
        return redisTemplate.opsForSet().pop("lottery:candidate", num);
    }

    /**
     * 添加候选人
     * @param candidates 候选人列表
     */
    @Override
    public void addCandidate(String[] candidates) {
        redisTemplate.opsForSet().add("lottery:candidate", candidates);
    }
}
