package com.yarda.redis.controller;

import com.yarda.redis.service.IHotNewsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 模拟热点新闻-list:lpush,ltrim,lrange
 * @author xuezheng
 * @version 1.0
 * @date 2022/4/6 18:56
 */
@RestController
@RequestMapping("/hotNews")
public class HotNewsController {

    @Resource
    private IHotNewsService hotNewsService;

    /**
     * 添加热点新闻
     */
    @PostMapping
    public String addHotNews(String news){
        return hotNewsService.addHotNews(news);
    }

    /**
     * 获取热点新闻列表（支持分页）
     */
    @GetMapping
    public List<String> getHotNewsList(Integer pageNum, Integer size){
        return hotNewsService.getHotNewsList(pageNum, size);
    }
}
