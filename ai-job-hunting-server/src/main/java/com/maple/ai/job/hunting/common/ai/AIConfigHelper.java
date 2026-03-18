package com.maple.ai.job.hunting.common.ai;

import com.maple.ai.job.hunting.config.AppBizConfig;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.function.Function;

/**
 * aiConfig 帮助程序
 *
 * @author gaoping
 * @since 2025/02/11
 */
@Component
public class AIConfigHelper {

    @Resource
    private AppBizConfig appBizConfig;


    @Nonnull
    public RestClient.Builder buildProxyRestClient() {
        HttpClient httpClient = HttpClientBuilder.create()
                .setProxy(new HttpHost(appBizConfig.getProxyHost(), appBizConfig.getProxyPort()))
                .disableCookieManagement().build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectTimeout(3000);
        requestFactory.setConnectionRequestTimeout(3000);
        return RestClient.builder().requestFactory(requestFactory);
    }


    @Nonnull
    public static RestClient.Builder buildRestClient(Integer timeoutSeconds) {
        // 1. 配置超时
        RequestConfig config = RequestConfig.custom()
                //.setConnectTimeout(Timeout.ofSeconds(1))
                //.setConnectionRequestTimeout(Timeout.ofSeconds(1))
                .setResponseTimeout(Timeout.ofSeconds(timeoutSeconds))
                .build();

        HttpClient httpClient = HttpClientBuilder.create()
                //.setProxy(new HttpHost(appBizConfig.getProxyHost(), appBizConfig.getProxyPort()))
                .setDefaultRequestConfig(config)
                .disableCookieManagement().build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectTimeout(5000);
        requestFactory.setConnectionRequestTimeout(5000);
        return RestClient.builder().requestFactory(requestFactory);
    }

    /**
     * 获取 AI 配置
     * 适配分隔符为[ . or - ]
     *
     * @param func 函数
     * @param key  钥匙
     * @return {@link R }
     */
    public <R> R getAiConfig(Function<String, R> func, String key) {
        R result = func.apply(key);
        if (result != null) {
            return result;
        }
        if (key.contains(".")) {
            return func.apply(key.replace(".", "-"));
        } else if (key.contains("-")) {
            return func.apply(key.replace("-", "."));
        }
        return null;
    }
}
