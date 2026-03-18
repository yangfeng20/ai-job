package com.maple.ai.job.hunting.controller;

import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.model.common.Response;
import com.maple.ai.job.hunting.service.biz.AIAssistantService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * @author maple
 * Created Date: 2024/4/23 14:03
 * Description:
 */

@RestController
@RequestMapping("/api/job/ai/assistant")
public class AIAssistantController {

    @Resource
    private AIAssistantService aiAssistantService;

    @PostMapping("/generate/greeting")
    public Response<String> generateGreeting() {
        String greeting = aiAssistantService.generateGreeting(HeaderContext.getHeader().getUserId());
        return Response.success(greeting);
    }
}
