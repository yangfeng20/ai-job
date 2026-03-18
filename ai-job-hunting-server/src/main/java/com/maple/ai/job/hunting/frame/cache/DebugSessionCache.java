package com.maple.ai.job.hunting.frame.cache;

import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author yangfeng
 * @date : 2023/11/29 17:25
 * desc:
 */

@Component
public class DebugSessionCache extends AbsMemoryCache<String, List<? extends Message>> {
    public DebugSessionCache() {
        super(10, TimeUnit.MINUTES, true);
    }
}
