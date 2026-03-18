package com.maple.ai.job.hunting.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 滤镜结果 vo
 *
 * @author gaoping
 * @since 2025/06/14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiFilterResultVO {

    /**
     * 是否过滤
     */
    private Boolean filter;

    /**
     * 过滤原因
     */
    private String reason;
}
