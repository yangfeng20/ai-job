package com.maple.ai.job.hunting.service.biz;

import com.maple.ai.job.hunting.frame.cache.DebugSessionCache;
import com.maple.ai.job.hunting.model.entity.PreferenceEntity;
import com.maple.ai.job.hunting.model.openai.OpenaiMessage;
import com.maple.ai.job.hunting.model.param.JobSeekerClonedParam;
import com.maple.ai.job.hunting.model.vo.DebugPromptVO;
import com.maple.ai.job.hunting.model.vo.JobSeekerClonedResultVO;
import com.maple.ai.job.hunting.service.ai.impl.DebugOpenAIPoolService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author maple
 * Created Date: 2024/5/14 14:43
 * Description:
 */

@Service
@Slf4j
public class DebugJobSeekerClonedService extends JobSeekerClonedService {

    @Resource
    private DebugSessionCache debugSessionCache;

    @Resource
    private DebugOpenAIPoolService debugOpenAIPoolService;

    @NotNull
    @Override
    protected String getOrInitSession(String jobKey, Long userId) {
        return jobKey;
    }

    @Override
    public JobSeekerClonedResultVO ask(JobSeekerClonedParam jobSeekerClonedParam) {
        DebugPromptVO debugParam = (DebugPromptVO) jobSeekerClonedParam;
        String systemPrompt = debugOpenAIPoolService.buildSystemFirstPromptWithUserPrompt(debugParam.getUserPrompt());

        // 调试模式下，每次使用最新的用户提示词等内容构建最新的系统提示词
        List<OpenaiMessage> messageList = new CopyOnWriteArrayList<>(debugParam.getMessageList());
        messageList.add(0, new OpenaiMessage(MessageType.SYSTEM, systemPrompt));

        debugSessionCache.set(jobSeekerClonedParam.getJobKey(), messageList);
        return super.ask(jobSeekerClonedParam);
    }


    @Override
    protected void sendInformEmail(JobSeekerClonedParam jobSeekerClonedParam, Optional<PreferenceEntity> preferenceEntityOpt, String answer, String jobKey, Boolean isHighInterest) {
        // 调试不发送邮件
    }
}
