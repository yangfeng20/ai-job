package com.maple.ai.job.hunting.frame.cache;

import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * OpenAI客户端缓存
 *
 * @author gaoping
 */
@Component
public class CustomOpenAiClientCache extends AbsMemoryCache<Long, OpenAiChatClient> {

    public CustomOpenAiClientCache() {
        // 设置1小时过期时间，重置过期时间
        super(6, TimeUnit.HOURS, true);
    }
}