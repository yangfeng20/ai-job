package com.maple.ai.job.hunting.common.ai;


import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maple.ai.job.hunting.frame.exp.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * 带有会话聊天路径的 OpenAIAPI
 *
 * @author gaoping
 * @see org.springframework.ai.openai.api.OpenAiApi#chatCompletionEntity
 * @since 2025/02/11
 */
@Slf4j
public class CompletionPathOpenAiApi extends OpenAiApi {

    private static final ObjectMapper SHARED_MAPPER = new ObjectMapper();

    static {
        SHARED_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private final String completionsPath;

    private final JSONObject extraBody;


    public CompletionPathOpenAiApi(String baseUrl, String openAiToken, String completionsPath, JSONObject extraBody) {
        this(baseUrl, openAiToken, completionsPath, RestClient.builder(), extraBody);
    }

    public CompletionPathOpenAiApi(String baseUrl, String openAiToken, String completionsPath, RestClient.Builder restClientBuilder) {
        this(baseUrl, openAiToken, completionsPath, restClientBuilder, RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER, null);
    }


    public CompletionPathOpenAiApi(String baseUrl, String openAiToken, String completionsPath, RestClient.Builder restClientBuilder, JSONObject extraBody) {
        this(baseUrl, openAiToken, completionsPath, restClientBuilder, RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER, extraBody);
    }

    public CompletionPathOpenAiApi(String baseUrl, String openAiToken, String completionsPath, RestClient.Builder restClientBuilder,
                                   ResponseErrorHandler responseErrorHandler, JSONObject extraBody) {
        super(baseUrl, openAiToken, restClientBuilder, responseErrorHandler);
        this.extraBody = extraBody;
        this.completionsPath = completionsPath;
    }

    /**
     * 聊天完成实体
     * 现在为了扩展参数都需要走新逻辑。如果没有配置配置completionsPath。则使用父类默认的地址
     *
     * @param chatRequest 聊天请求
     * @return {@link ResponseEntity }<{@link OpenAiApi.ChatCompletion }>
     */
    @Override
    public ResponseEntity<OpenAiApi.ChatCompletion> chatCompletionEntity(OpenAiApi.ChatCompletionRequest chatRequest) {
        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(!chatRequest.stream(), "Request must set the steam property to false.");

        RestClient restClient = getSupperRestClient();
        ResponseEntity<String> responseEntity = restClient.post()
                .uri(Optional.ofNullable(completionsPath).map(String::trim).orElse("/v1/chat/completions"))
                .body(buildBody(chatRequest))
                .retrieve()
                .toEntity(String.class);

        String body = responseEntity.getBody();

        if (body == null || body.trim().isEmpty()) {
            log.error("AI API returned empty body. Status: {}, URL: {}", responseEntity.getStatusCode(), completionsPath);
            throw new ApplicationException("AI API returned empty body");
        }

        try {
            OpenAiApi.ChatCompletion chatCompletion = SHARED_MAPPER.readValue(body, OpenAiApi.ChatCompletion.class);
            return ResponseEntity.status(responseEntity.getStatusCode()).body(chatCompletion);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response: {}", body);
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }

    private RestClient getSupperRestClient() {
        RestClient restClient;
        try {
            Field restClientField = OpenAiApi.class.getDeclaredField("restClient");
            restClientField.setAccessible(true);
            restClient = (RestClient) restClientField.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("无法创建RestClient", e);
        }
        return restClient;
    }

    private Object buildBody(OpenAiApi.ChatCompletionRequest chatRequest) {
        if (extraBody == null) {
            return chatRequest;
        }
        JSONObject bodyJsonObj;
        try {
            bodyJsonObj = BeanUtil.copyProperties(extraBody, JSONObject.class);
        } catch (Exception e) {
            log.error("扩展body解析失败 body:{}", extraBody, e);
            return chatRequest;
        }

        String originalBodyStr;
        try {
            originalBodyStr = SHARED_MAPPER.writeValueAsString(chatRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("无法序列化body", e);
        }
        JSONObject originalBodyJsonObj = JSON.parseObject(originalBodyStr);
        bodyJsonObj.putAll(originalBodyJsonObj);

        return bodyJsonObj.toJSONString();
    }
}
