package com.maple.ai.job.hunting.service.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.consts.AIPromptStrConstant;
import com.maple.ai.job.hunting.mapper.MsgSessionMapper;
import com.maple.ai.job.hunting.mapper.UserAIConfigMapper;
import com.maple.ai.job.hunting.mapper.UserInfoMapper;
import com.maple.ai.job.hunting.mapper.UserResumeMapper;
import com.maple.ai.job.hunting.model.bo.UserAIConfigDO;
import com.maple.ai.job.hunting.model.bo.UserResumeDO;
import com.maple.ai.job.hunting.model.entity.PreferenceEntity;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author maple
 * Created Date: 2024/5/15 11:14
 * Description:
 */

public abstract class AbstractAIService implements AIService {

    @Resource
    protected MsgSessionMapper msgSessionMapper;
    @Resource
    protected UserResumeMapper userResumeMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserAIConfigMapper userAIConfigMapper;


    public String buildSystemFirstPrompt() {
        return buildSystemFirstPromptWithUserPrompt(null);
    }

    public String buildSystemFirstPromptWithUserPrompt(String userPromptOverride) {
        // 载入简历，作为系统提示上下文
        LambdaQueryWrapper<UserResumeDO> condition = new LambdaQueryWrapper<>();
        condition.eq(UserResumeDO::getUserId, HeaderContext.getHeader().getUserId());
        UserResumeDO userResumeDO = userResumeMapper.selectOne(condition);
        Map<String, String> promptMap = Arrays.stream(AIPromptStrConstant.AI_SEAT_SYSTEM_PROMPT.split(AIPromptStrConstant.SPLIT_SYMBOL))
                .map(s -> AIPromptStrConstant.SPLIT_SYMBOL + s).collect(Collectors.toMap(s -> s.split("\n")[0], s -> s));

        String systemPrompt = promptMap.get(AIPromptStrConstant.ROLE_SETTING_TITLE);
        // 用户自定义提示词
        UserAIConfigDO userAIConfigDO = userAIConfigMapper.selectByUserId(HeaderContext.getHeader().getUserId());
        String userPrompt = Optional.ofNullable(userAIConfigDO).map(UserAIConfigDO::getUserPrompt).orElse("");

        // 载入偏好设置，作为系统提示上下文
        PreferenceEntity preferenceEntity = Optional.ofNullable(userInfoMapper.selectPreferenceByUserId(HeaderContext.getHeader().getUserId()))
                .orElse(new PreferenceEntity());

        // 预设问题
        String presetProblem = "";
        if (preferenceEntity.isPpE() && StringUtils.isNotBlank(preferenceEntity.getPp())) {
            presetProblem = promptMap.get(AIPromptStrConstant.PRESET_PROBLEM_TITLE) + preferenceEntity.getPp();
        }

        // 拒绝挽留
        String rejectFuse = "";
        if (preferenceEntity.isRfE() && StringUtils.isNotBlank(preferenceEntity.getRf())) {
            rejectFuse = promptMap.get(AIPromptStrConstant.REJECT_FUSE_TITLE);
        }

        // 用户提示词
        userPrompt = StringUtils.defaultIfBlank(userPromptOverride, userPrompt);
        if (StringUtils.isNotBlank(userPrompt)) {
            userPrompt = AIPromptStrConstant.USER_PROMPT_TITLE + "\n" + userPrompt + "\n\n";
        }

        return joinAndWrap(systemPrompt, presetProblem, rejectFuse, userPrompt, AIPromptStrConstant.RESUME_TITLE, userResumeDO.getResumeContent());
    }

    private String joinAndWrap(String... strArr) {
        List<String> filtered = Arrays.stream(strArr)
                .filter(StringUtils::isNotBlank)
                .toList();

        if (filtered.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        result.append(filtered.get(0));
        boolean prevEndsWithNewline = filtered.get(0).endsWith("\n");

        for (int i = 1; i < filtered.size(); i++) {
            String current = filtered.get(i);
            boolean currentStartsWithNewline = current.startsWith("\n");
            boolean currentEndsWithNewline = current.endsWith("\n");

            // 如果前一个字符串结尾或当前字符串开头有换行，则不添加额外换行符
            if (!prevEndsWithNewline && !currentStartsWithNewline) {
                result.append("\n");
            }
            result.append(current);
            prevEndsWithNewline = currentEndsWithNewline;
        }

        return result.toString();
    }

}
