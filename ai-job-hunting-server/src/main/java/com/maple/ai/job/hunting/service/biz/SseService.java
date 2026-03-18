package com.maple.ai.job.hunting.service.biz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author maple
 * Created Date: 2024/5/15 21:22
 * Description:
 */

@Slf4j
@Service
public class SseService {

    private final Map<Long, Sinks.Many<String>> sseUserSinkMap = new ConcurrentHashMap<>();


    // 用于向客户端发送数据
    public void notifyClient(Long userId, String message) {
        Sinks.Many<String> sink = sseUserSinkMap.get(userId);
        if (sink == null) {
            log.error("sse推送消息用户关联sse为空 userId:{} message:{}", userId, message);
            return;
        }
        sink.tryEmitNext(message);
    }

    public Sinks.Many<String> getSink(Long userId) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        sseUserSinkMap.put(userId, sink);
        return sink;
    }
}
