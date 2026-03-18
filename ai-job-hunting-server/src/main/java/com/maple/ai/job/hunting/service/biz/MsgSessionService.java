package com.maple.ai.job.hunting.service.biz;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.emums.AiSeatStatusEnum;
import com.maple.ai.job.hunting.emums.MsgSessionStatusEnum;
import com.maple.ai.job.hunting.entity.MsgSessionDO;
import com.maple.ai.job.hunting.frame.cache.MsgSessionStopCache;
import com.maple.ai.job.hunting.frame.cache.SessionKeyIdCache;
import com.maple.ai.job.hunting.frame.exp.ApplicationException;
import com.maple.ai.job.hunting.mapper.MsgSessionMapper;
import com.maple.ai.job.hunting.mapper.UserInfoMapper;
import com.maple.ai.job.hunting.model.bo.UserInfoDO;
import com.maple.ai.job.hunting.service.ai.impl.OpenAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @author maple
 * Created Date: 2024/5/14 14:43
 * Description:
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class MsgSessionService {

    private final @Nonnull MsgSessionStopCache msgSessionStopCache;

    private final @Nonnull MsgSessionMapper msgSessionMapper;

    private final @Nonnull UserInfoMapper userInfoMapper;

    private final @Nonnull SessionKeyIdCache sessionKeyIdCache;

    @Resource(name = "openai")
    private OpenAIService openAIService;


    private boolean globalStop;


    public boolean msgSessionStop(String jobKey) {
        if (globalStop) {
            return true;
        }

        // 兼容调试
        if (jobKey != null && jobKey.contains("ask-debug-")) {
            return false;
        }

        Long userId = HeaderContext.getHeader().getUserId();
        UserInfoDO userInfoDO = userInfoMapper.selectById(userId);
        // 用户全局停止时，停止会话
        if (!AiSeatStatusEnum.OPEN.getCode().equals(userInfoDO.getAiSeatStatus())) {
            return true;
        }

        Boolean isStop = msgSessionStopCache.get(jobKey);
        return isStop != null && isStop;
    }

    public boolean stopUserAllSession(Long userId) {
        // 查询出当前用户的所有会话（最近使用，活跃）
        LambdaQueryWrapper<MsgSessionDO> condition = new LambdaQueryWrapper<>();
        condition.eq(MsgSessionDO::getUserId, userId);
        condition.eq(MsgSessionDO::getStatus, MsgSessionStatusEnum.IN_USE.getCode());
        condition.select(MsgSessionDO::getSessionKey);
        List<MsgSessionDO> sessionDOList = msgSessionMapper.selectList(condition);
        sessionDOList.stream().map(MsgSessionDO::getSessionKey).forEach(jobKey -> msgSessionStopCache.set(jobKey, true));
        return true;
    }

    public boolean setMsgSessionStop(String jobKey, Boolean isStop) {
        if ("globalJobKey".equals(jobKey)) {
            globalStop = isStop;
        }


        // 检测是否有会话记录，避免还没有是
        if (!existMsgSession(jobKey)) {
            throw new ApplicationException("会话不存在;无需修改状态", 200);
        }

        if (Boolean.TRUE.equals(isStop) && !"globalJobKey".equals(jobKey)) {
            Optional.ofNullable(userInfoMapper.selectById(HeaderContext.getHeader().getUserId()))
                    .map(UserInfoDO::getAiSeatStatus)
                    .filter(status -> !AiSeatStatusEnum.OPEN.getCode().equals(status)).ifPresent(status -> {
                        throw new ApplicationException("全局AI坐席开关已关闭");
                    });
        }

        return msgSessionStopCache.set(jobKey, isStop);
    }

    @SuppressWarnings("all")
    public boolean trySetMsgSessionStop(String jobKey, Boolean isStop) {
        boolean result = false;
        try {
            result = setMsgSessionStop(jobKey, isStop);
        } catch (Exception e) {
            log.error("设置会话停止失败", e);
        }
        return result;
    }


    private boolean existMsgSession(String jobKey) {

        String sessionId = sessionKeyIdCache.get(jobKey);
        if (StringUtils.isBlank(sessionId)) {
            return false;
        }

        return openAIService.getMsgSessionMap().containsKey(sessionId);
    }
}
