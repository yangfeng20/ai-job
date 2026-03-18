package com.maple.ai.job.hunting.service.ai.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.maple.ai.job.hunting.config.ai.KimiAIConfig;
import com.maple.ai.job.hunting.consts.AIPromptStrConstant;
import com.maple.ai.job.hunting.emums.AiFileResolveResultTypeEnum;
import com.maple.ai.job.hunting.model.AiFileResolveResult;
import com.maple.ai.job.hunting.service.ai.helper.MoonshotAiUtils;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author maple
 * Created Date: 2024/5/8 14:45
 * Description:
 */

@Slf4j
@Service("kimi")
public class KimiAIService extends OpenAIService {

    @Resource
    private KimiAIConfig.KimiAIChatClient kimiAIChatClient;

    @Resource
    private MoonshotAiUtils moonshotAiUtils;

    @Override
    protected OpenAiChatClient getClient() {
        return kimiAIChatClient;
    }

    @Override
    public void init() {
        // 避免重复执行父类的init
        //super.init();

        // 定时清理kimi文件(限制1000个文件)
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new BasicThreadFactory.Builder().namingPattern("kimi-file-clear-%d").build());
        executor.scheduleAtFixedRate(this::clearFile, 0, 30, TimeUnit.MINUTES);
    }

    public void clearFile() {
        try {
            JSONArray fileList = moonshotAiUtils.getFileList();
            if (fileList.size() < 500) {
                log.info("kimi 文件数量小于500 不需要清理");
                return;
            }

            fileList.stream().limit(10).map(item -> ((JSONObject) item).getStr("id")).forEach(fileId -> {
                JSONObject deleted = moonshotAiUtils.deleteFile(fileId);
                log.info("kimi 清理文件 id:{} result:{}", fileId, deleted);
            });
        } catch (Exception e) {
            log.error("执行 kimi 文件清理任务失败: {}", e.getMessage());
        }
    }

    @SneakyThrows
    @Override
    public AiFileResolveResult readFile(InputStream inputStream, String ask) {
        JSONObject uploadResult = moonshotAiUtils.uploadFile(IOUtils.toByteArray(inputStream));
        JSONObject fileContentResult = moonshotAiUtils.getFileContent(uploadResult.getStr("id"));
        return AiFileResolveResult.builder()
                .fileId(uploadResult.getStr("id"))
                .resolveResultType(AiFileResolveResultTypeEnum.UNRESOLVED)
                .originalFileContent(fileContentResult.getStr("content"))
                .resolveResult(null)
                .extra(null)
                .build();
    }

    /**
     * 总结简历
     * 调用ai总结简历，需要消耗token，总token大概2000左右【根据简历文字数量】
     *
     * @param fileContent 文件内容
     * @return {@link String}
     */
    @Deprecated
    private String summarizeResume(String fileContent) {
        List<Message> messageList = new ArrayList<>();
        messageList.add(new SystemMessage(fileContent));
        messageList.add(new UserMessage(AIPromptStrConstant.AI_SEAT_SYSTEM_PROMPT));
        Prompt prompt = new Prompt(messageList);
        ChatResponse chatResponse = kimiAIChatClient.call(prompt);
        return chatResponse.getResult().getOutput().getContent();
    }
}
