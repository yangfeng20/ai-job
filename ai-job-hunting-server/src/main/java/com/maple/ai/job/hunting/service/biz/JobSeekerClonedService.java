package com.maple.ai.job.hunting.service.biz;

import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.emums.AIResultCommandEnum;
import com.maple.ai.job.hunting.emums.BossOperationTypeEnum;
import com.maple.ai.job.hunting.emums.JobSeekerClonedAnswerTypeEnum;
import com.maple.ai.job.hunting.emums.MsgSessionStatusEnum;
import com.maple.ai.job.hunting.entity.MsgSessionDO;
import com.maple.ai.job.hunting.frame.cache.HighInterestInformCache;
import com.maple.ai.job.hunting.frame.cache.QuestionCache;
import com.maple.ai.job.hunting.frame.cache.SessionKeyIdCache;
import com.maple.ai.job.hunting.mapper.MsgSessionMapper;
import com.maple.ai.job.hunting.mapper.UserAIConfigMapper;
import com.maple.ai.job.hunting.mapper.UserInfoMapper;
import com.maple.ai.job.hunting.model.ChatSessionResult;
import com.maple.ai.job.hunting.model.entity.PreferenceEntity;
import com.maple.ai.job.hunting.model.entity.SendEmailEntity;
import com.maple.ai.job.hunting.model.param.JobSeekerClonedParam;
import com.maple.ai.job.hunting.model.vo.JobSeekerClonedResultVO;
import com.maple.ai.job.hunting.service.ai.AIServiceFacade;
import com.maple.ai.job.hunting.service.ai.impl.CustomOpenAIService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author maple
 * Created Date: 2024/5/14 14:43
 * Description:
 */

@Service
@Slf4j
public class JobSeekerClonedService {

    @Resource
    private MsgSessionMapper msgSessionMapper;
    @Resource
    private AIServiceFacade aiServiceFacade;
    @Resource
    private UserAIConfigMapper userAIConfigMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private CustomOpenAIService customOpenAIService;
    @Resource
    private EmailService emailService;
    @Resource
    private MsgSessionService msgSessionService;
    @Resource
    private HighInterestInformCache highInterestInformCache;
    @Resource
    private SessionKeyIdCache sessionKeyIdCache;
    @Resource
    private QuestionCache questionCache;

    public JobSeekerClonedResultVO ask(JobSeekerClonedParam jobSeekerClonedParam) {
        String jobKey = jobSeekerClonedParam.getJobKey();
        Long userId = HeaderContext.getHeader().getUserId();

        // 停止会话
        if (msgSessionService.msgSessionStop(jobKey)) {
            return new JobSeekerClonedResultVO(JobSeekerClonedAnswerTypeEnum.STOP, "");
        }
        // 重复问题过滤
        if (askDuplication(jobSeekerClonedParam)) {
            return new JobSeekerClonedResultVO(JobSeekerClonedAnswerTypeEnum.STOP, "");
        }
        // 前置过滤处理
        Pair<Boolean, JobSeekerClonedResultVO> pair = preAskFilter(jobSeekerClonedParam);
        if (pair.getKey()) {
            log.info("预过滤 jobKey:{} userId:{} question:{} result:{}", jobSeekerClonedParam.getJobKey(),
                    HeaderContext.getHeader().getUserId(), jobSeekerClonedParam.getQuestion(), pair.getValue());
            return pair.getValue();
        }

        // 获取或初始化会话
        String sessionId = getOrInitSession(jobKey, userId);

        // 获取ai回复
        ChatSessionResult sessionChat = aiServiceFacade.sessionChat(sessionId, jobSeekerClonedParam.getQuestion());
        if (JobSeekerClonedAnswerTypeEnum.AI_SERVICE_EXCEPTION.equals(sessionChat.getAnswerStatus())) {
            return new JobSeekerClonedResultVO(JobSeekerClonedAnswerTypeEnum.AI_SERVICE_EXCEPTION, "");
        }

        String answer = sessionChat.getAnswer();
        log.info("ai回复 sessionId:{} jobKey:{} userId:{} question:{} answer:{}", sessionId, jobKey, userId, jobSeekerClonedParam.getQuestion(), answer);

        Optional<PreferenceEntity> preferenceEntityOpt = userInfoMapper.selectPreferenceOptByUserId(userId);
        log.info("偏好设置 sessionId:{} jobKey:{} userId:{} preferenceEntityOpt:{}", sessionId, jobKey, userId, preferenceEntityOpt);

        // 开启了拒绝挽留
        boolean rfEnable = preferenceEntityOpt.filter(PreferenceEntity::isRfE).filter(p -> StringUtils.isNotBlank(p.getRf())).isPresent();
        if (rfEnable && hitRfAndGet(answer)) {
            log.info("拒绝挽留 sessionId:{} jobKey:{} userId:{}", sessionId, jobKey, userId);
            List<Integer> answerTypeList = List.of(JobSeekerClonedAnswerTypeEnum.MSG_TEXT.getCode(), JobSeekerClonedAnswerTypeEnum.BOSS_OPERATION.getCode());
            return new JobSeekerClonedResultVO(answerTypeList, preferenceEntityOpt.get().getRf(), Collections.singletonList(BossOperationTypeEnum.SEND_RESUME.getCode()));
        }

        Boolean highInterest = highInterest(jobSeekerClonedParam, preferenceEntityOpt, jobKey);
        boolean enableHighInterestAiStop = preferenceEntityOpt.filter(PreferenceEntity::isHiaE).isPresent();
        if (highInterest && enableHighInterestAiStop) {
            log.info("高意向停止AI坐席 sessionId:{} jobKey:{} userId:{}", sessionId, jobKey, userId);
            msgSessionService.trySetMsgSessionStop(jobKey, true);
            sendInformEmail(jobSeekerClonedParam, preferenceEntityOpt, "<高意向停止AI坐席>", jobKey, true);
            return new JobSeekerClonedResultVO(JobSeekerClonedAnswerTypeEnum.STOP, "");
        }

        List<Integer> answerTypeList = Lists.newArrayList(JobSeekerClonedAnswerTypeEnum.MSG_TEXT.getCode());
        List<Integer> operationTypeList = Lists.newArrayList();
        if (answer.contains(AIResultCommandEnum.SEND_RESUME.getDesc())) {
            answerTypeList.remove(JobSeekerClonedAnswerTypeEnum.MSG_TEXT.getCode());
            answerTypeList.add(JobSeekerClonedAnswerTypeEnum.BOSS_OPERATION.getCode());
            operationTypeList.add(BossOperationTypeEnum.SEND_RESUME.getCode());
            answer = "";
        }

        answer = postProcessAiAnswer(answer);
        // 发送通知邮件
        sendInformEmail(jobSeekerClonedParam, preferenceEntityOpt, answer, jobKey, highInterest);
        return new JobSeekerClonedResultVO(answerTypeList,
                Optional.ofNullable(answer).map(String::trim).orElse(answer), operationTypeList);
    }

    /**
     * 是否命中 拒绝挽留
     * 大模型有时候并不能完全处理，提示词，大模型能力问题，都可能不完全按照意向处理
     *
     * @return boolean
     */
    private boolean hitRfAndGet(String aiAnswer) {
        if (StringUtils.isBlank(aiAnswer)) {
            return false;
        }
        boolean contains1 = aiAnswer.contains(AIResultCommandEnum.HR_REJECT.getDesc());
        boolean contains2 = aiAnswer.contains("拒绝处理");

        return contains1 || contains2;
    }

    private String postProcessAiAnswer(String aiAnswer) {
        if (StringUtils.isBlank(aiAnswer)) {
            return aiAnswer;
        }
        // 垃圾ai，发送简历应该返回命令 【COMMAND_SEND_RESUME】 结果有时候给我返回 【COMAND_SEND_RESUME】
        if (aiAnswer.startsWith("COM") && (aiAnswer.endsWith("REJECT") || aiAnswer.endsWith("RESUME"))) {
            return "";
        }
        return aiAnswer.replaceAll("【拒绝处理】|拒绝处理| |空格", "");
    }

    private Pair<Boolean, JobSeekerClonedResultVO> preAskFilter(JobSeekerClonedParam jobSeekerClonedParam) {
        String question = jobSeekerClonedParam.getQuestion();
        if (StringUtils.isBlank(question)) {
            return Pair.of(true, new JobSeekerClonedResultVO(JobSeekerClonedAnswerTypeEnum.STOP, ""));
        }
        boolean contains1 = question.contains("对方已同意，您的附件简历已发送给对方");
        if (contains1) {
            return Pair.of(true, new JobSeekerClonedResultVO(JobSeekerClonedAnswerTypeEnum.STOP, ""));
        }

        // 核心逻辑：匹配【你|您】的简历 并且排除【前有"感谢/抱歉"】且【后有"不太合适/不合适"】
        boolean contains2 = question.matches("^(?!(.*?(感谢|抱歉))).*?[您你]的简历(?!(.*?(不太合适|不合适))).*+");
        if (contains2) {
            List<Integer> answerTypeList = List.of(JobSeekerClonedAnswerTypeEnum.BOSS_OPERATION.getCode());
            return Pair.of(true, new JobSeekerClonedResultVO(answerTypeList, "", Collections.singletonList(BossOperationTypeEnum.SEND_RESUME.getCode())));
        }
        return Pair.of(false, null);
    }

    /**
     * 高意向
     *
     * @param jobSeekerClonedParam 求职者克隆参数
     * @param preferenceEntityOpt  首选项实体 opt
     * @param jobKey               Job 键
     * @return {@link Pair }<{@link Boolean },{@link Boolean }>
     */
    private Boolean highInterest(JobSeekerClonedParam jobSeekerClonedParam, Optional<PreferenceEntity> preferenceEntityOpt, String jobKey) {
        // 高意向邮件通知
        boolean enableHighInterest = preferenceEntityOpt.filter(PreferenceEntity::isCrE).isPresent();
        // 高意向AI坐席停止
        boolean enableHighInterestAiStop = preferenceEntityOpt.filter(PreferenceEntity::isHiaE).isPresent();
        Boolean currentUserHaveNotified = highInterestInformCache.get(HeaderContext.getHeader().getUserId() + jobKey);
        boolean isHighInterest = false;
        if ((enableHighInterest || enableHighInterestAiStop) && !Boolean.TRUE.equals(currentUserHaveNotified)) {
            List<String> keyList = preferenceEntityOpt.map(PreferenceEntity::getCrK).orElse(Collections.emptyList());
            if (keyList.contains(jobSeekerClonedParam.getQuestion())) {
                isHighInterest = true;
            } else {
                // 对话轮数高意向判断（未命中关键词才判断，避免重复判断，消耗资源）
                MsgSessionDO msgSessionDO = msgSessionMapper.selectBySessionKey(jobKey);
                if (preferenceEntityOpt.get().getCrC() != null && getChatRounds(msgSessionDO) >= preferenceEntityOpt.get().getCrC()) {
                    isHighInterest = true;
                }
            }
        }

        return isHighInterest;
    }

    protected void sendInformEmail(JobSeekerClonedParam jobSeekerClonedParam, Optional<PreferenceEntity> preferenceEntityOpt,
                                 String answer, String jobKey, Boolean isHighInterest) {
        // 每轮通话邮件通知
        if (preferenceEntityOpt.filter(PreferenceEntity::isErmE).isPresent()) {
            SendEmailEntity sendEmailEntity = new SendEmailEntity();
            HashMap<String, Object> param = new HashMap<>();
            param.put("answer", answer);
            param.put("jobName", jobSeekerClonedParam.getJobInfo().getString("jobTitle"));
            param.put("question", jobSeekerClonedParam.getQuestion());
            sendEmailEntity.setParamMap(param);
            sendEmailEntity.setSubject("每轮对话邮件通知");
            sendEmailEntity.setTemplateName("chat_rounds.html");
            sendEmailEntity.setToSendUserSet(Collections.singleton(HeaderContext.getHeader().getUserId()));
            emailService.sendEmail(sendEmailEntity);
            log.info("每轮通话邮件 jobKey:{}", jobKey);
        }

        // 高意向邮件通知
        boolean enableHighInterest = preferenceEntityOpt.filter(PreferenceEntity::isCrE).isPresent();
        if (isHighInterest && enableHighInterest) {
            SendEmailEntity sendEmailEntity = new SendEmailEntity();
            HashMap<String, Object> param = new HashMap<>();
            param.put("jobName", jobSeekerClonedParam.getJobInfo().getString("jobTitle"));
            sendEmailEntity.setParamMap(param);
            sendEmailEntity.setSubject("高意向邮件通知");
            sendEmailEntity.setTemplateName("intention_job.html");
            sendEmailEntity.setToSendUserSet(Collections.singleton(HeaderContext.getHeader().getUserId()));
            log.info("高意向邮件 jobKey:{}", jobKey);
            emailService.sendEmail(sendEmailEntity);
            highInterestInformCache.set(HeaderContext.getHeader().getUserId() + jobKey, Boolean.TRUE);
        }

    }

    /**
     * 获取对话轮数
     *
     * @param msgSessionDO msgSessionDO
     * @return int
     */
    private int getChatRounds(MsgSessionDO msgSessionDO) {
        if (msgSessionDO == null || StringUtils.isBlank(msgSessionDO.getMsgContext())) {
            return 0;
        }

        String msgContext = msgSessionDO.getMsgContext();
        JSONArray sessionMsgRecordJson = JSON.parseArray(msgContext);
        return (int) sessionMsgRecordJson.stream().filter(item -> {
            String role = ((JSONObject) item).getString("role");
            // 以ai输出的次数作为对话轮数【gemini使用model作为模型角色，其他ai都兼容openai的规范，使用assistant】
            return "model".equals(role) || "assistant".equals(role);
        }).count();


    }

    protected @NotNull String getOrInitSession(String jobKey, Long userId) {
        LambdaQueryWrapper<MsgSessionDO> condition = new LambdaQueryWrapper<>();
        condition.eq(MsgSessionDO::getSessionKey, jobKey);
        condition.eq(MsgSessionDO::getUserId, userId);
        condition.eq(MsgSessionDO::getStatus, MsgSessionStatusEnum.IN_USE.getCode());
        MsgSessionDO msgSessionDO = msgSessionMapper.selectOne(condition);
        String sessionId;
        if (msgSessionDO == null) {
            MsgSessionDO sessionDO = new MsgSessionDO(null, null, MsgSessionStatusEnum.IN_USE.getCode(), userId, jobKey);
            msgSessionMapper.insert(sessionDO);
            sessionId = sessionDO.getId().toString();
            log.info("initSession sessionId:{} jobKey:{} userId:{}", sessionId, jobKey, userId);
        } else {
            sessionId = msgSessionDO.getId().toString();
        }
        sessionKeyIdCache.set(jobKey, sessionId);
        return sessionId;
    }

    /**
     * 询问重复
     *
     * @param jobSeekerClonedParam 求职者克隆参数
     * @return boolean
     */
    private boolean askDuplication(JobSeekerClonedParam jobSeekerClonedParam) {
        String jobKey = jobSeekerClonedParam.getJobKey();
        String question = Optional.ofNullable(jobSeekerClonedParam.getQuestion()).orElse("").hashCode() + "";

        String key = jobKey + "-" + question;

        if (questionCache.contains(key)) {
            return true;
        }
        questionCache.set(key, "");
        return false;
    }
}
