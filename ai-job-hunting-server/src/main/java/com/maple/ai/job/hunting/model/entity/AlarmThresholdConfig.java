package com.maple.ai.job.hunting.model.entity;

import lombok.Data;

/**
 * 告警阈值配置
 * 例如：{"count":3,"minutes":1}
 * 一分钟这个时间窗口内，出现错误超过3次则会告警
 *
 * @author Gemini
 * @since 2025-11-20
 */
@Data
public class AlarmThresholdConfig {

    /**
     * 触发告警的次数阈值
     */
    private Integer count;

    /**
     * 时间窗口，单位：分钟
     */
    private Integer minutes;
}
