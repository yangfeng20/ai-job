package com.maple.ai.job.hunting.emums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI提供商枚举
 *
 * @author gaoping
 * @since 2025/04/11
 */
@Getter
@AllArgsConstructor
public enum AIProviderEnum {

    CUSTOM(0, "自定义", null),
    DEEPSEEK(1, "DeepSeek", "https://api.deepseek.com/v1"),
    VOLCANO(2, "火山引擎", "https://ark.cn-beijing.volces.com/api/v3"),
    SILICON_FLOW(3, "硅基流动", "https://api.siliconflow.cn/v1"),
    KIMI(4, "月之暗面", "https://api.moonshot.cn/v1"),
    OPEN_ROUTER(5, "OpenRouter", "https://openrouter.ai/api/v1"),
    ;

    private final Integer code;
    private final String desc;
    private final String defaultBaseUrl;

    public static AIProviderEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AIProviderEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    public boolean isCustom() {
        return CUSTOM.equals(this);
    }
}
