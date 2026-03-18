package com.maple.ai.job.hunting.config;

import com.maple.smart.config.core.annotation.JsonValue;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * @author maple
 * Created Date: 2024/5/8 14:34
 * Description:
 */

@Getter
@Configuration
public class AppBizConfig {

    @Value("${proxy_host:127.0.0.1}")
    private String proxyHost;

    @Value("${proxy_port:7890}")
    private Integer proxyPort;

    /**
     * 不同场景使用的AI
     * {
     *   "ask": "openai-pool",
     *   "session": "openai-pool",
     *   "file": "kimi"
     * }
     */
    @JsonValue("${use-ai-map:{\"ask\":\"openai-pool\",\"session\":\"openai-pool\",\"file\":\"kimi\"}}")
    private Map<String, String> sceneUseAiMap;

    @JsonValue("${admin.userId.list:[1]}")
    private List<Long> adminUserIdList;

    @JsonValue("${product.trial.count.map:{\"1\":5,\"6\":1}}")
    private Map<Integer, Integer> productTrialCountMap;

}
