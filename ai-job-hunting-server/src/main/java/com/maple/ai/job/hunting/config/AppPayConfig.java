package com.maple.ai.job.hunting.config;

import com.maple.ai.job.hunting.model.entity.GenerateOrderGroupConfigEntity;
import com.maple.smart.config.core.annotation.JsonValue;
import com.maple.smart.config.core.annotation.SmartValue;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author maple
 * Created Date: 2024/5/8 17:00
 * Description:
 */

@Getter
@Configuration
public class AppPayConfig {

    /**
     * 支付宝分配给开发者的应用ID。
     */
    @Value("${alipay.app_id:}")
    private String appId;

    /**
     * 商户的私钥，由支付宝提供。
     */
    @Value("${alipay.merchant_private_key:}")
    private String merchantPrivateKey;

    /**
     * 支付宝公钥，由支付宝提供。
     */
    @Value("${alipay.alipay_public_key:}")
    private String alipayPublicKey;

    /**
     * 支付宝异步通知回调地址。
     */
    @Value("${alipay.notify_url:}")
    private String notifyUrl;

    /**
     * 编码格式。
     */
    @Value("${alipay.charset:}")
    private String charset;

    /**
     * 支付宝网关地址。
     */
    @Value("${alipay.gatewayUrl:}")
    private String gatewayUrl;

    /**
     * LOGO base64
     */
    @SmartValue("${alipay.logo.base64:}")
    private String logoBase64;

    @JsonValue("${generate.order.group.config:" + GenerateOrderGroupConfigEntity.DEFAULT_CONFIG + "}")
    private GenerateOrderGroupConfigEntity generateOrderGroupConfig;


    public String getLogoBase64() {
        if (logoBase64 == null || logoBase64.isEmpty()) {
            return logoBase64;
        }
        if (logoBase64.startsWith("data:image/png;base64,")) {
            return logoBase64.replace("data:image/png;base64,", "");
        }
        return logoBase64;
    }
}
