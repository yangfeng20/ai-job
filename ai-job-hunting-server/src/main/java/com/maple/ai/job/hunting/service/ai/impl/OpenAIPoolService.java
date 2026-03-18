package com.maple.ai.job.hunting.service.ai.impl;

import com.maple.ai.job.hunting.config.ai.OpenAIPoolConfig;
import jakarta.annotation.Resource;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.stereotype.Service;

/**
 * openai ai能力池
 *
 * @author maple
 * @since 2025/02/11
 */
@Service("openai-pool")
public class OpenAIPoolService  extends OpenAIService {

    @Resource
    private OpenAIPoolConfig.OpenAIPoolManager openAIPoolManager;


    @Override
    public void init() {
        // 避免重复执行父类的init
        //super.init();
    }

    @Override
    protected OpenAiChatClient getClient() {
        return openAIPoolManager.getClient();
    }
}
