package com.yarda.redis.service.impl;

import com.yarda.redis.service.IHotNewsService;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xuezheng
 * @version 1.0
 * @date 2022/4/6 19:08
 */
@Service
public class HotNewsServiceImpl implements IHotNewsService {

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 添加热点新闻
     * @param news 热点新闻
     * @return 添加结果
     */
    @Override
    public String addHotNews(String news) {
        Long hotNews = redisTemplate.opsForList().rightPush("hotNews", news);
        // 控制热点新闻列表内容不超过30
        if(hotNews > 30){
            redisTemplate.opsForList().trim("hotNews", 0, 29);
        }
        return "success";
    }

    /**
     * 获取热点新闻列表
     * @param pageNum 当前页
     * @param size 每页条数
     * @return 热点新闻列表
     */
    @Override
    public List<String> getHotNewsList(Integer pageNum, Integer size) {
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        int start = (pageNum-1) * size;
        int end = pageNum * size -1;
        return listOperations.range("hotNews", start, end);
    }
}
