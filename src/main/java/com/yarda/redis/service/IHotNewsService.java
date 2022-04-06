package com.yarda.redis.service;

import java.util.List;

/**
 * @author xuezheng
 * @version 1.0
 * @date 2022/4/6 19:05
 */
public interface IHotNewsService {
    /**
     * 添加热点新闻
     * @param news 热点新闻
     * @return 添加结果
     */
    String addHotNews(String news);

    /**
     * 获取热点新闻列表
     * @param pageNum 当前页
     * @param size 每页条数
     * @return 热点新闻列表
     */
    List<String> getHotNewsList(Integer pageNum, Integer size);
}
