package com.maple.ai.job.hunting.service.biz;

import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.common.ai.AIConfigHelper;
import com.maple.ai.job.hunting.common.ai.CompletionPathOpenAiApi;
import com.maple.ai.job.hunting.emums.AIProviderEnum;
import com.maple.ai.job.hunting.emums.BizCodeEnum;
import com.maple.ai.job.hunting.emums.ProductTypeEnum;
import com.maple.ai.job.hunting.frame.cache.CustomOpenAiClientCache;
import com.maple.ai.job.hunting.frame.exp.AIPowerException;
import com.maple.ai.job.hunting.frame.exp.ApplicationException;
import com.maple.ai.job.hunting.mapper.UserAIConfigMapper;
import com.maple.ai.job.hunting.model.bo.UserAIConfigDO;
import com.maple.ai.job.hunting.model.vo.DebugPromptVO;
import com.maple.ai.job.hunting.model.vo.JobSeekerClonedResultVO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

/**
 * 用户AI配置服务
 *
 * @author gaoping
 * @since 2025/04/11
 */
@Service
public class UserAIConfigService {

    @Resource
    private UserAIConfigMapper userAIConfigMapper;

    @Resource
    private CustomOpenAiClientCache customOpenAiClientCache;

    @Resource
    private DebugJobSeekerClonedService debugJobSeekerClonedService;

    /**
     * 保存配置
     */
    public Boolean save(UserAIConfigDO config) {
        UserAIConfigDO existConfig = userAIConfigMapper.selectByUserId(config.getUserId());
        if (existConfig == null) {
            return userAIConfigMapper.insert(config) > 0;
        } else {
            config.setId(existConfig.getId());
            // 允许编辑用户提示词等动态字段
            if (config.getUserPrompt() == null) {
                config.setUserPrompt(existConfig.getUserPrompt());
            }
            customOpenAiClientCache.delete(config.getUserId());
            return userAIConfigMapper.updateById(config) > 0;
        }
    }

    /**
     * 获取当前用户配置
     */
    public UserAIConfigDO getCurrentConfig() {
        return userAIConfigMapper.selectByUserId(HeaderContext.getHeader().getUserId());
    }

    /**
     * 测试配置
     */
    public String testUserApiConfig(UserAIConfigDO config) {

        try {
            validateConfig(config, false);
            OpenAiChatClient client = buildOpenAiChatClient(config);

            // 发送测试请求
            String response = client.call("你好，你是");
            if (StringUtils.isBlank(response)) {
                throw new ApplicationException("响应为空");
            }

            return response;
        } catch (Exception e) {
            throw new ApplicationException("测试失败 " + e.getMessage(), BizCodeEnum.AI_CONFIG_TEST_FAILED.getCode());
        }
    }

    public boolean openCustomApi(Set<Integer> productPowerList) {
        if (!productPowerList.contains(ProductTypeEnum.CUSTOM_API.getCode())) {
            return false;
        }
        UserAIConfigDO currentConfig = this.getCurrentConfig();
        return Optional.ofNullable(currentConfig).filter(c -> c.getStatus() == 1).isPresent();
    }

    /**
     * 禁用配置
     */
    public Boolean disable(Long id) {
        UserAIConfigDO config = userAIConfigMapper.selectById(id);
        if (config == null) {
            throw new ApplicationException("配置不存在");
        }
        if (!config.getUserId().equals(HeaderContext.getHeader().getUserId())) {
            throw new ApplicationException("无权修改此配置");
        }

        config.setStatus(0);
        return userAIConfigMapper.updateById(config) > 0;
    }

    public void validateConfig(UserAIConfigDO config, boolean checkTestPassed) {
        if (config == null) {
            throw new AIPowerException("custom", "未配置AI服务");
        }

        if (StringUtils.isBlank(config.getApiKey())) {
            throw new AIPowerException("custom", "未配置API密钥");
        }
        if (checkTestPassed && config.getTestPassed() != 1) {
            throw new AIPowerException("custom", "配置未通过测试，请先测试配置");
        }

        if (config.getProvider() == null) {
            throw new AIPowerException("custom", "未配置API提供商");
        }

        if (StringUtils.isBlank(config.getModelName())) {
            throw new AIPowerException("custom", "未配置模型名称");
        }

        AIProviderEnum provider = AIProviderEnum.getByCode(config.getProvider());
        if (provider == null) {
            throw new AIPowerException("custom", "无效的API提供商");
        }

        if (provider.isCustom() && StringUtils.isBlank(config.getBaseUrl())) {
            throw new AIPowerException("custom", "自定义API提供商必须配置baseUrl");
        }
    }

    public static @NotNull OpenAiChatClient buildOpenAiChatClient(UserAIConfigDO config) {
        String baseUrl = config.getBaseUrl();

        // 根据大模型提供商，选择baseUrl
        if (StringUtils.isBlank(baseUrl)) {
            AIProviderEnum provider = AIProviderEnum.getByCode(config.getProvider());
            if (provider != null && provider.getDefaultBaseUrl() != null) {
                baseUrl = provider.getDefaultBaseUrl();
            }
        }

        // 创建新的配置
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(config.getModelName())
                .build();

        /**
         * completionsPath 大模型提供商的baseUrl结尾都已经包含版本了。所以path要移除版本。
         * @see OpenAiApi#chatCompletionEntity(OpenAiApi.ChatCompletionRequest)
         */
        String completionsPath = StringUtils.defaultIfBlank(config.getCompletionsPath(), "/chat/completions");

        CompletionPathOpenAiApi openAiApi = new CompletionPathOpenAiApi(
                baseUrl,
                config.getApiKey(),
                completionsPath,
                AIConfigHelper.buildRestClient(config.getTimeout())
        );
        return new OpenAiChatClient(openAiApi, options);
    }

    public JobSeekerClonedResultVO debugUserPrompt(DebugPromptVO debugPromptVO) {
        return debugJobSeekerClonedService.ask(debugPromptVO);
    }
}
