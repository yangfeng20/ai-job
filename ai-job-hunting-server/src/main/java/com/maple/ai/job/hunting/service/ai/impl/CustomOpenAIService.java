package com.maple.ai.job.hunting.service.ai.impl;

import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.frame.cache.CustomOpenAiClientCache;
import com.maple.ai.job.hunting.frame.cache.DebugSessionCache;
import com.maple.ai.job.hunting.mapper.UserAIConfigMapper;
import com.maple.ai.job.hunting.model.bo.UserAIConfigDO;
import com.maple.ai.job.hunting.service.biz.UserAIConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 自定义OpenAI服务实现
 *
 * @author gaoping
 * @since 2025/04/11
 */
@Slf4j
@Service("customOpenAI")
public class CustomOpenAIService extends OpenAIService {

    @Resource
    private CustomOpenAiClientCache clientCache;

    @Resource
    private UserAIConfigMapper userAIConfigMapper;

    @Resource
    private UserAIConfigService userAIConfigService;

    @Resource
    private DebugSessionCache debugSessionCache;

    @Override
    protected OpenAiChatClient getClient() {
        Long userId = HeaderContext.getHeader().getUserId();
        OpenAiChatClient client = clientCache.get(userId);

        // 构建client缓存
        if (client == null) {
            UserAIConfigDO config = getUserConfig();
            userAIConfigService.validateConfig(config, true);
            client = UserAIConfigService.buildOpenAiChatClient(config);
            clientCache.set(userId, client);
        }
        return client;
    }


    private UserAIConfigDO getUserConfig() {
        Long userId = HeaderContext.getHeader().getUserId();
        return userAIConfigMapper.getEnabledConfig(userId);
    }


    @SuppressWarnings("unchecked")
    @Override
    public Map<String, List<Message>> getMsgSessionMap() {
        if (!isDebug()) {
            return super.getMsgSessionMap();
        }
        Map<String, List<? extends Message>> map = debugSessionCache.toMap();
        // 使用类型安全的转换方式
        return (Map<String, List<Message>>) (Map<?, ?>) map;
    }

    @Override
    protected void persistentMessage(String sessionId, List<Message> messageList) {
        if (isDebug()) {
            return;
        }
        super.persistentMessage(sessionId, messageList);
    }

    private boolean isDebug() {
        return Optional.ofNullable(HeaderContext.getHeader().getExtend()).map(map -> map.get("debug")).map(Boolean.class::cast).orElse(false);
    }
}
