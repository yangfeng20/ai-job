package com.maple.ai.job.hunting.service.biz;

import com.alibaba.fastjson.JSON;
import com.maple.ai.job.hunting.consts.AIPromptStrConstant;
import com.maple.ai.job.hunting.model.param.AIFilterParam;
import com.maple.ai.job.hunting.model.vo.AiFilterResultVO;
import com.maple.ai.job.hunting.service.ai.AIServiceFacade;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Job Filter 服务
 *
 * @author gaoping
 * @since 2025/06/14
 */
@Service
public class JobFilterService {

    private static final Logger log = LoggerFactory.getLogger(JobFilterService.class);
    @Resource
    private AIServiceFacade aiServiceFacade;

    private static final Pattern JSON_PATTERN = Pattern.compile("\\{.*}", Pattern.DOTALL);

    public AiFilterResultVO filter(AIFilterParam param) {
        String answer = aiServiceFacade.askAndAnswer(buildQuestion(param));
        if (StringUtils.isBlank(answer)) {
            log.warn("AI过滤结果为空");
            return AiFilterResultVO.builder().filter(false).reason("AI过滤结果为空").build();
        }

        // 提取第一个 JSON 对象
        Matcher matcher = JSON_PATTERN.matcher(answer);
        if (matcher.find()) {
            String json = matcher.group();
            try {
                return JSON.parseObject(json, AiFilterResultVO.class);
            } catch (Exception e) {
                log.error("AI过滤结果 JSON 解析失败:{}", answer);
            }
        } else {
            log.error("AI过滤结果 不包含有效JSON:{}", answer);
        }

        return AiFilterResultVO.builder().filter(false).reason("AI过滤结果解析失败").build();
    }



    public String buildQuestion(AIFilterParam param) {
        return AIPromptStrConstant.AI_FILTER_SYSTEM_PROMPT +
                "\n用户筛选条件：\n" + param.getPrompt() +
                "\n岗位基本信息：\n" + param.getJobBaseInfo() +
                "\n岗位扩展信息：\n" + param.getJobExtInfo();
    }
}
