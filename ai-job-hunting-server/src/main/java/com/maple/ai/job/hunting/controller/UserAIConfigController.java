package com.maple.ai.job.hunting.controller;

import cn.hutool.core.bean.BeanUtil;
import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.emums.AIProviderEnum;
import com.maple.ai.job.hunting.model.bo.UserAIConfigDO;
import com.maple.ai.job.hunting.model.common.Response;
import com.maple.ai.job.hunting.model.vo.AIProviderVO;
import com.maple.ai.job.hunting.model.vo.DebugPromptVO;
import com.maple.ai.job.hunting.model.vo.JobSeekerClonedResultVO;
import com.maple.ai.job.hunting.model.vo.UserAIConfigVO;
import com.maple.ai.job.hunting.service.biz.UserAIConfigService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 用户AI配置控制器
 *
 * @author gaoping
 * @since 2025/04/11
 */
@Validated
@RestController
@RequestMapping("/api/user/ai/config")
public class UserAIConfigController {

    @Resource
    private UserAIConfigService userAIConfigService;

    /**
     * 保存配置
     */
    @PostMapping("/save")
    public Response<Boolean> save(@Valid @RequestBody UserAIConfigVO config) {
        config.setUserId(HeaderContext.getHeader().getUserId());
        return Response.success(userAIConfigService.save(BeanUtil.copyProperties(config, UserAIConfigDO.class)));
    }

    /**
     * 临时保存配置
     */
    @PostMapping("/temp/save")
    public Response<Boolean> tempSave(@Valid @RequestBody UserAIConfigVO config) {
        config.setUserId(HeaderContext.getHeader().getUserId());
        // 只有在临时保存自定义ai配置是，才将测试状态修改为未通过。在保存用户提示词时，不需要测试状态
        if (config.getUserPrompt() == null) {
            config.setTestPassed(0);
        }
        return Response.success(userAIConfigService.save(BeanUtil.copyProperties(config, UserAIConfigDO.class)));
    }

    /**
     * 获取当前用户配置
     */
    @GetMapping("/current")
    public Response<UserAIConfigVO> getCurrentConfig() {
        return Response.success(BeanUtil.copyProperties(userAIConfigService.getCurrentConfig(), UserAIConfigVO.class));
    }

    /**
     * 测试配置
     */
    @PostMapping("/test")
    public Response<String> testConfig(@Valid @RequestBody UserAIConfigVO config) {
        return Response.success(userAIConfigService.testUserApiConfig(BeanUtil.copyProperties(config, UserAIConfigDO.class)));
    }

    /**
     * 禁用配置
     */
    @PostMapping("/disable/{id}")
    public Response<Boolean> disable(@PathVariable("id") Long id) {
        return Response.success(userAIConfigService.disable(id));
    }

    /**
     * 调试用户提示词，返回当前系统提示词拼接结果
     */
    @PostMapping("/debug")
    public Response<JobSeekerClonedResultVO> debugUserPrompt(@Valid @RequestBody DebugPromptVO debugPromptVO) {
        return Response.success(userAIConfigService.debugUserPrompt(debugPromptVO));
    }

    /**
     * 获取AI供应商详情
     */
    @Deprecated
    @GetMapping("/provider/{code}")
    public Response<AIProviderVO> getProviderDetail(@PathVariable("code") Integer code) {
        return Response.success(BeanUtil.copyProperties(AIProviderEnum.getByCode(code), AIProviderVO.class));
    }

    /**
     * 获取AI供应商详情
     */

    @GetMapping("/all/provider")
    public Response<List<AIProviderVO>> getAllProviderDetail() {
        return Response.success(Arrays.stream(AIProviderEnum.values())
                .map(e -> BeanUtil.copyProperties(e, AIProviderVO.class))
                .toList());
    }
}
