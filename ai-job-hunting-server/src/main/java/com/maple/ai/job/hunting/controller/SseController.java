package com.maple.ai.job.hunting.controller;

import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.service.biz.SseService;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * @author maple
 */
@Controller
@RequestMapping("/api/sse")
public class SseController {


    @Resource
    private SseService sseService;

    /**
     * 建立sse连接
     *
     * @return {@link Flux}<{@link ServerSentEvent}<{@link String}>>
     */
    @GetMapping("/connect")
    public Flux<ServerSentEvent<String>> sse() {
        Long userId = HeaderContext.getHeader().getUserId();
        return sseService.getSink(userId).asFlux()
                .map(e -> ServerSentEvent.<String>builder().id(UUID.randomUUID().toString()).event("message").data(e).build());
    }
}