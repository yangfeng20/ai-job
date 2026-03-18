package com.maple.ai.job.hunting.model.vo;

import lombok.Data;

/**
 * 用户 ai config vo
 *
 * @author gaoping
 * @since 2025/04/11
 */
@Data
public class UserAIConfigVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 提供商类型
     *
     * @see com.maple.ai.job.hunting.emums.AIProviderEnum
     */
    private Integer provider;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 基础URL
     */
    private String baseUrl;

    /**
     * 超时时间(秒)
     */
    private Integer timeout;

    /**
     * 接口路径
     */
    private String completionsPath;

    /**
     * 测试是否通过 0-未通过 1-已通过
     */
    private Integer testPassed;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 用户自定义提示词
     */
    private String userPrompt;

}
