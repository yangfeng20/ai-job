package com.maple.ai.job.hunting.service.ai;

import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.config.AppBizConfig;
import com.maple.ai.job.hunting.emums.JobSeekerClonedAnswerTypeEnum;
import com.maple.ai.job.hunting.emums.ProductTypeEnum;
import com.maple.ai.job.hunting.frame.exp.AIPowerException;
import com.maple.ai.job.hunting.frame.exp.ApplicationException;
import com.maple.ai.job.hunting.mapper.UserAIConfigMapper;
import com.maple.ai.job.hunting.model.AiFileResolveResult;
import com.maple.ai.job.hunting.model.ChatSessionResult;
import com.maple.ai.job.hunting.model.bo.UserAIConfigDO;
import com.maple.ai.job.hunting.model.entity.AlarmThresholdConfig;
import com.maple.ai.job.hunting.model.entity.SendEmailEntity;
import com.maple.ai.job.hunting.service.ai.impl.CustomOpenAIService;
import com.maple.ai.job.hunting.service.biz.EmailService;
import com.maple.ai.job.hunting.service.biz.ProductService;
import com.maple.smart.config.core.annotation.JsonValue;
import com.maple.ai.job.hunting.service.biz.SlidingWindowAlarmService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author maple
 * Created Date: 2024/4/27 23:21
 * Description:
 */

@Slf4j
@Service
public class AIServiceFacade {

    @Resource
    private AppBizConfig appBizConfig;

    @Resource
    private Map<String, AIService> aiServiceMap;

    @Resource
    private EmailService emailService;

    @Resource
    private CustomOpenAIService customOpenAIService;

    @Resource
    private UserAIConfigMapper userAIConfigMapper;

    @Resource
    private SlidingWindowAlarmService slidingWindowAlarmService;

    @Resource
    private ProductService productService;

    @JsonValue("${ai.alarm.threshold.map:{\"def\":{\"count\":3,\"minutes\":1}}}")
    private Map<String, AlarmThresholdConfig> aiAlarmThresholdMap;

    @Value("${ai.error.email:true}")
    private Boolean aiErrorEmail;

    @Value("${ai.error.user.email:true}")
    private Boolean aiErrorUserEmail;

    @Nonnull
    public ChatSessionResult sessionChat(String sessionId, String ask) {
        if (sessionId != null && sessionId.contains("ask-debug-")) {
            Map<String, Object> extMap = Optional.ofNullable(HeaderContext.getHeader().getExtend()).orElse(new HashMap<>());
            extMap.put("debug", Boolean.TRUE);
            HeaderContext.getHeader().setExtend(extMap);
            return debugSessionChat(sessionId, ask);
        }
        AIService aiService = aiServiceMap.get(appBizConfig.getSceneUseAiMap().getOrDefault("session", "openai-pool"));
        ChatSessionResult chatSessionResult = new ChatSessionResult();
        try {
            // 检查是否有自定义AI配置
            UserAIConfigDO config = userAIConfigMapper.getEnabledConfig(HeaderContext.getHeader().getUserId());
            if (config != null) {
                // 使用自定义配置
                chatSessionResult = customOpenAIService.sessionChat(sessionId, ask);
            } else if (productService.hasProductAbility(HeaderContext.getHeader().getUserId(), ProductTypeEnum.AI_SEAT.getCode())) {
                chatSessionResult = aiService.sessionChat(sessionId, ask);
            } else {
                throw new ApplicationException("未开启自有API或AI坐席已过期");
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("未开启自有API或AI坐席已过期")) {
                throw e;
            }
            aiPowerExpAlarm(e);
            log.error("sessionChat error sessionId:{} error:{}", sessionId, e);
            chatSessionResult.setAnswerStatus(JobSeekerClonedAnswerTypeEnum.AI_SERVICE_EXCEPTION);
            return chatSessionResult;
        }
        return chatSessionResult;
    }


    @Nonnull
    public ChatSessionResult debugSessionChat(String sessionId, String ask) {
        AIService aiService = aiServiceMap.get("openai-debug");
        ChatSessionResult chatSessionResult = new ChatSessionResult();
        try {
            // 检查是否有自定义AI配置
            UserAIConfigDO config = userAIConfigMapper.getEnabledConfig(HeaderContext.getHeader().getUserId());
            if (config != null) {
                // 使用自定义配置
                chatSessionResult = customOpenAIService.sessionChat(sessionId, ask);
            } else if (productService.hasProductAbility(HeaderContext.getHeader().getUserId(), ProductTypeEnum.AI_SEAT.getCode())) {
                chatSessionResult = aiService.sessionChat(sessionId, ask);
            } else {
                throw new ApplicationException("未开启自有API或AI坐席已过期");
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("未开启自有API或AI坐席已过期")) {
                throw e;
            }
            log.error("debugSessionChat error sessionId:{} error:{}", sessionId, e);
            chatSessionResult.setAnswerStatus(JobSeekerClonedAnswerTypeEnum.AI_SERVICE_EXCEPTION);
            return chatSessionResult;
        }
        return chatSessionResult;
    }


    public String askAndAnswer(String ask) {
        AIService aiService = aiServiceMap.get(appBizConfig.getSceneUseAiMap().get("ask"));
        String reuslt = null;
        try {
            UserAIConfigDO config = userAIConfigMapper.getEnabledConfig(HeaderContext.getHeader().getUserId());
            if (config != null) {
                reuslt = customOpenAIService.askAndAnswer(ask);
                // AI过滤和ai招呼语的能力同时存在，仅判断一个就好
            } else if (productService.hasProductAbilityAll(HeaderContext.getHeader().getUserId(), ProductTypeEnum.AI_SEAT, ProductTypeEnum.AI_FILTER)) {
                reuslt = aiService.askAndAnswer(ask);
            } else {
                throw new ApplicationException("未开启自有API或产品能力已过期");
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("未开启自有API或产品能力已过期")) {
                throw e;
            }
            aiPowerExpAlarm(e);
            log.error("askAndAnswer error userId:{} error:{}", HeaderContext.getHeader().getUserId(), e);
        }
        return reuslt;
    }


    public AiFileResolveResult readFile(InputStream inputStream, String ask) {
        AIService aiService = aiServiceMap.get(appBizConfig.getSceneUseAiMap().getOrDefault("file", "kimi"));
        return aiService.readFile(inputStream, ask);
    }

    /**
     * AI Power EXP 告警
     *
     * @param e e
     */
    private void aiPowerExpAlarm(Exception e) {
        if (e instanceof AIPowerException aiPowerException) {
            Long userId = HeaderContext.getHeader().getUserId();
            String exceptionName = aiPowerException.getName();
            String key = aiPowerException.getKey().endsWith("-")
                    ? aiPowerException.getKey() + userId
                    : aiPowerException.getKey();
            log.error("aiPower error {}", key);

            // Get alarm configuration
            AlarmThresholdConfig thresholdConfig = getAlarmThreshold(aiPowerException);
            Integer thresholdCount = thresholdConfig.getCount();
            Integer thresholdMinutes = thresholdConfig.getMinutes();

            int currentCountInWindow = slidingWindowAlarmService.recordAndGetCount(exceptionName, thresholdMinutes);

            // System-level alarm, triggers only when the threshold is first crossed
            if (currentCountInWindow >= thresholdCount && aiErrorEmail) {
                emailService.sendEmail(SendEmailEntity.builder()
                        .subject("AI服务异常通知")
                        .templateName("ai-error-email.html")
                        .toSendUserSet(new HashSet<>(appBizConfig.getAdminUserIdList()))
                        .paramMap(Map.of("count", currentCountInWindow, "msg", e.getMessage(), "name", key))
                        .build());
            }

            // User-facing alarm for their custom API key, may trigger repeatedly
            if (aiErrorEmail && aiErrorUserEmail && key.contains("unknown") && currentCountInWindow >= thresholdCount) {
                emailService.sendEmail(SendEmailEntity.builder()
                        .subject("AI工作猎手")
                        .templateName("custom-ai-error-email.html")
                        .toSendUserSet(Set.of(userId))
                        .paramMap(Map.of("count", currentCountInWindow, "msg", e.getMessage(), "name", key))
                        .build());
            }
        }
    }

    private AlarmThresholdConfig getAlarmThreshold(AIPowerException aiPowerException) {
        AlarmThresholdConfig defConfig = aiAlarmThresholdMap.get("def");
        // Failsafe in case the default config from JSON is not loaded
        if (defConfig == null) {
            defConfig = new AlarmThresholdConfig();
            defConfig.setCount(3);
            defConfig.setMinutes(1); // Correct failsafe
        }

        AlarmThresholdConfig specificConfig = aiAlarmThresholdMap.get(aiPowerException.getName());

        // If no specific config, return the default
        if (specificConfig == null) {
            return defConfig;
        }

        // If specific config exists, merge it with default
        // If specific value is null, use default value
        if (specificConfig.getCount() == null) {
            specificConfig.setCount(defConfig.getCount());
        }
        if (specificConfig.getMinutes() == null) {
            specificConfig.setMinutes(defConfig.getMinutes());
        }

        return specificConfig;
    }

}
