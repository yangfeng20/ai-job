package com.maple.ai.job.hunting.service.ai.impl;

import com.maple.ai.job.hunting.frame.cache.DebugSessionCache;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * openai ai能力池
 *
 * @author maple
 * @since 2025/02/11
 */
@Service("openai-debug")
public class DebugOpenAIPoolService extends OpenAIPoolService {

    @Resource
    private DebugSessionCache debugSessionCache;


    @Override
    public void init() {
        // 避免重复执行父类的init
        //super.init();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, List<Message>> getMsgSessionMap() {
        Map<String, List<? extends Message>> map = debugSessionCache.toMap();
        // 使用类型安全的转换方式
        return (Map<String, List<Message>>) (Map<?, ?>) map;
    }

    @Override
    protected void persistentMessage(String sessionId, List<Message> messageList) {

    }
}
