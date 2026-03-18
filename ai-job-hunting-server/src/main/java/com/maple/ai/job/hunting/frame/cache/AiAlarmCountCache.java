package com.maple.ai.job.hunting.frame.cache;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI告警次数
 *
 * @author maple
 * @since 2024-04-27 22:52
 */

@Component
public class AiAlarmCountCache extends AbsMemoryCache<String, List<Long>> {
    public AiAlarmCountCache() {
        super(1, TimeUnit.DAYS, false);
    }
}
