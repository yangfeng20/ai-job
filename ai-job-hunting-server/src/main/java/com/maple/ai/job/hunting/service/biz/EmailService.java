package com.maple.ai.job.hunting.service.biz;

import cn.hutool.extra.mail.MailUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maple.ai.job.hunting.mapper.UserInfoMapper;
import com.maple.ai.job.hunting.model.bo.UserInfoDO;
import com.maple.ai.job.hunting.model.entity.SendEmailEntity;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 邮件服务
 *
 * @author maple
 * @since 2024/06/26
 */
@Service
public class EmailService {

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private UserInfoMapper userInfoMapper;

    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(2, 2,
            0L, java.util.concurrent.TimeUnit.MILLISECONDS, new java.util.concurrent.LinkedBlockingQueue<>(6));


    public void sendEmail(SendEmailEntity sendEmailEntity) {
        // 构建邮件内容
        Context context = new Context(Locale.CHINESE, sendEmailEntity.getParamMap());
        String emailContent = templateEngine.process(sendEmailEntity.getTemplateName(), context);

        // 构建收件人集合
        Set<String> toEmailSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(sendEmailEntity.getToSendUserSet())) {
            LambdaQueryWrapper<UserInfoDO> condition = new LambdaQueryWrapper<>();
            condition.in(UserInfoDO::getId, sendEmailEntity.getToSendUserSet());
            Set<String> userEmailSets = userInfoMapper.selectList(condition).stream().map(UserInfoDO::getEmail).collect(Collectors.toSet());
            toEmailSet.addAll(userEmailSets);
        }
        if (CollectionUtils.isNotEmpty(sendEmailEntity.getToSendEmailSet())) {
            toEmailSet.addAll(sendEmailEntity.getToSendEmailSet());
        }

        // 异步发送邮件
        THREAD_POOL_EXECUTOR.submit(() -> {
            MailUtil.send(toEmailSet, sendEmailEntity.getSubject(), emailContent, true);
        });
    }
}
