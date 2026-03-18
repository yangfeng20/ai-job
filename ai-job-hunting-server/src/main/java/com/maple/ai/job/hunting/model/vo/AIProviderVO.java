package com.maple.ai.job.hunting.model.vo;

import lombok.Data;

/**
 * AI供应商VO
 *
 * @author gaoping
 * @since 2025/04/11
 */
@Data
public class  AIProviderVO {

    /**
     * 供应商编码
     */
    private Integer code;

    /**
     * 供应商描述
     */
    private String desc;

    /**
     * 默认基础URL
     */
    private String defaultBaseUrl;
}