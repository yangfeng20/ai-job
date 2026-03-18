package com.maple.ai.job.hunting.frame.cache;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author yangfeng
 * @date : 2023/11/29 17:25
 * desc:
 */

@Component
public class SessionKeyIdCache extends AbsMemoryCache<String, String> {
    public SessionKeyIdCache() {
        super(10, TimeUnit.HOURS, true);
    }
}
