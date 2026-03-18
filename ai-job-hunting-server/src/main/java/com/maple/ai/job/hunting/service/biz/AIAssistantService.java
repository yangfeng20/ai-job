package com.maple.ai.job.hunting.service.biz;


import com.maple.ai.job.hunting.consts.AIPromptStrConstant;
import com.maple.ai.job.hunting.frame.exp.ApplicationException;
import com.maple.ai.job.hunting.mapper.UserResumeMapper;
import com.maple.ai.job.hunting.model.bo.UserResumeDO;
import com.maple.ai.job.hunting.service.ai.AIServiceFacade;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * AIAssistant 服务
 *
 * @author gaoping
 * @since 2025/03/12
 */
@Service
@RequiredArgsConstructor
public class AIAssistantService {


    private final @Nonnull AIServiceFacade aiServiceFacade;

    private final @Nonnull UserResumeMapper userResumeMapper;

    public String generateGreeting(Long userId) {
        UserResumeDO userResumeDO = userResumeMapper.selectByUserId(userId);
        if (Objects.isNull(userResumeDO)){
            throw new ApplicationException("请先导入简历");
        }
        String prompt = AIPromptStrConstant.GREETING + userResumeDO.getResumeContent();
        return aiServiceFacade.askAndAnswer(prompt);
    }
}
