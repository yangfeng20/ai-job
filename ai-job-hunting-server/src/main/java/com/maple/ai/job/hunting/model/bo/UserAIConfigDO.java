package com.maple.ai.job.hunting.model.bo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.maple.ai.job.hunting.model.common.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户AI配置
 *
 * @author gaoping
 * @since 2025/04/13
 */
@Data
@TableName("user_ai_config")
@EqualsAndHashCode(callSuper = true)
public class UserAIConfigDO extends BaseDO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 提供商类型
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
