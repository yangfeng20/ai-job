package com.maple.ai.job.hunting;

import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.model.entity.SendEmailEntity;
import com.maple.ai.job.hunting.service.biz.EmailService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.HashMap;

@SpringBootTest(classes = AiJobHuntingApplication.class)
public class EmailTest {

    @Resource
    private EmailService emailService;

    @Test
    public void sendEmail() {
        SendEmailEntity sendEmailEntity = new SendEmailEntity();
        HashMap<String, Object> param = new HashMap<>();
        param.put("answer", "回答的内容");
        param.put("jobName", "javaName");
        param.put("question", "提问");
        sendEmailEntity.setParamMap(param);
        sendEmailEntity.setSubject("每轮对话邮件通知");
        sendEmailEntity.setTemplateName("chat_rounds.html");
        sendEmailEntity.setToSendUserSet(Collections.singleton(1L));
        emailService.sendEmail(sendEmailEntity);
    }
}
