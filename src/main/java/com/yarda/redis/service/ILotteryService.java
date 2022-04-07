package com.yarda.redis.service;

import java.util.List;

/**
 * @author xuezheng
 * @version 1.0
 * @date 2022/4/7 18:42
 */
public interface ILotteryService {
    /**
     * 随机抽取获奖幸运儿
     * @return 幸运儿列表
     * @param num 抽取数量
     */
    List<String> getLuckyPerson(Integer num);

    /**
     * 添加候选人
     * @param candidates 候选人列表
     */
    void addCandidate(String[] candidates);
}
