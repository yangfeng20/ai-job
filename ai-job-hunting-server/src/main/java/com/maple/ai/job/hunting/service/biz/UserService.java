package com.maple.ai.job.hunting.service.biz;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.config.AppBizConfig;
import com.maple.ai.job.hunting.consts.AIPromptStrConstant;
import com.maple.ai.job.hunting.consts.RePatternConstant;
import com.maple.ai.job.hunting.emums.AiSeatStatusEnum;
import com.maple.ai.job.hunting.emums.BizCodeEnum;
import com.maple.ai.job.hunting.emums.ProductTypeEnum;
import com.maple.ai.job.hunting.frame.cache.ProductNotAuthorizedCache;
import com.maple.ai.job.hunting.frame.cache.SysSessionCache;
import com.maple.ai.job.hunting.frame.exp.ApplicationException;
import com.maple.ai.job.hunting.mapper.UserInfoMapper;
import com.maple.ai.job.hunting.mapper.UserResumeMapper;
import com.maple.ai.job.hunting.mapper.UserTrialMapper;
import com.maple.ai.job.hunting.model.AiFileResolveResult;
import com.maple.ai.job.hunting.model.bo.UserInfoDO;
import com.maple.ai.job.hunting.model.bo.UserResumeDO;
import com.maple.ai.job.hunting.model.vo.UserInfoVO;
import com.maple.ai.job.hunting.service.ai.AIServiceFacade;
import com.maple.ai.job.hunting.utils.FileTypeDetector;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

/**
 * @author maple
 * Created Date: 2024/5/9 16:24
 * Description:
 */

@Slf4j
@Service
public class UserService {
    @Resource
    private AIServiceFacade aiServiceFacade;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserResumeMapper userResumeMapper;
    @Resource
    private SysSessionCache sysSessionCache;

    @Resource
    private ProductService productService;

    @Resource
    private UserTrialMapper userTrialMapper;

    @Resource
    private AppBizConfig appBizConfig;

    @Resource
    private UserAIConfigService userAIConfigService;

    @Resource
    private ProductNotAuthorizedCache productNotAuthorizedCache;

    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(2, 2,
            60L, TimeUnit.MINUTES, new java.util.concurrent.LinkedBlockingQueue<>(500));

    public UserInfoVO importResume(InputStream resumeFileStream, String uniqueId, String resumeId) throws Exception {
        byte[] bytes = resumeFileStream.readAllBytes();
        InputStream inputStream1 = new ByteArrayInputStream(bytes);
        InputStream inputStream2 = new ByteArrayInputStream(bytes);

        StopWatch watch = new StopWatch("导入简历");
        // 1. ai读取总结简历文件
        watch.start("AI识别简历");
        AiFileResolveResult aiFileResolveResult = aiServiceFacade.readFile(inputStream1, AIPromptStrConstant.AI_SEAT_SYSTEM_PROMPT);
        String summarize = aiFileResolveResult.getOriginalFileContent();
        watch.stop();

        // 2. 正则匹配邮箱手机
        watch.start("正则匹配邮箱手机");
        UserInfoVO result = parsePhoneAndEmail(summarize);
        watch.stop();

        // 3. 保存用户信息
        watch.start("保存用户信息");
        String ossFileName = "简历-" + uniqueId + "-" + DateUtil.formatDate(new Date()) + "." + FileTypeDetector.detectFileType(bytes);
        saveUserInfo(uniqueId, resumeId, result, summarize, ossFileName);
        watch.stop();
        return new UserInfoVO(result.getPhone(), result.getEmail());
    }

    private void saveUserInfo(String uniqueId, String resumeId, UserInfoVO result, String summarize, String ossFileName) {
        UserInfoDO dbUserInfo = userInfoMapper.selectByUniqueId(uniqueId);
        if (dbUserInfo == null) {
            UserInfoDO userInfoDO = new UserInfoDO(result.getPhone(), result.getEmail(), uniqueId);
            userInfoMapper.insert(userInfoDO);
            userResumeMapper.insert(new UserResumeDO(userInfoDO.getId(), summarize, ossFileName, resumeId));
        } else {
            LambdaQueryWrapper<UserResumeDO> condition = new LambdaQueryWrapper<>();
            condition.eq(UserResumeDO::getUserId, dbUserInfo.getId());
            userResumeMapper.delete(condition);
            userResumeMapper.insert(new UserResumeDO(dbUserInfo.getId(), summarize, ossFileName, resumeId));
            LambdaUpdateWrapper<UserInfoDO> updateWrapper = new LambdaUpdateWrapper<UserInfoDO>()
                    .eq(UserInfoDO::getId, dbUserInfo.getId())
                    .set(UserInfoDO::getUniqueId, uniqueId);
            if (StringUtils.isNotBlank(result.getEmail())) {
                updateWrapper.set(UserInfoDO::getEmail, result.getEmail());
            }
            if (StringUtils.isNotBlank(result.getPhone())) {
                updateWrapper.set(UserInfoDO::getPhone, result.getPhone());
            }
            userInfoMapper.update(new UserInfoDO(), updateWrapper);
        }
    }

    public String loginSilently(String uniqueId) {
        UserInfoDO userInfoDO = userInfoMapper.selectByUniqueId(uniqueId);
        if (userInfoDO == null) {
            throw new ApplicationException(BizCodeEnum.USER_NOT_EXIST);
        }

        String loginKey = Base64.encode(uniqueId + ":" + DateUtil.formatDateTime(new Date()));
        sysSessionCache.set(loginKey, BeanUtil.copyProperties(userInfoDO, UserInfoVO.class));
        return loginKey;
    }

    public UserInfoVO getUserInfo() {
        Long userId = HeaderContext.getHeader().getUserId();
        if (userId == null) {
            return null;
        }
        UserInfoDO userInfoDO = userInfoMapper.selectById(userId);
        UserResumeDO userResumeDO = userResumeMapper.selectOne(new LambdaQueryWrapper<UserResumeDO>().eq(UserResumeDO::getUserId, userInfoDO.getId()));
        UserInfoVO userInfoVO = BeanUtil.copyProperties(userInfoDO, UserInfoVO.class);
        userInfoVO.setPreference(JSONUtil.parseObj(userInfoDO.getPreference()));
        Optional.ofNullable(userResumeDO).ifPresent(resumeDO -> userInfoVO.setResumeId(resumeDO.getResumeId()));

        //购买过ai坐席，查询是否过期
        if (Objects.nonNull(userInfoDO.getAiSeatStatus())) {
            boolean openAiSeat = AiSeatStatusEnum.OPEN.getCode().equals(userInfoDO.getAiSeatStatus());
            if (openAiSeat) {
                // 开启了ai坐席，但是不存在有效期内的AI坐席产品
                Set<Integer> userProductSet = productService.queryUserValidAllProductType(userId);
                boolean existAiSeat = userProductSet.contains(ProductTypeEnum.AI_SEAT.getCode());
                boolean openCustomApi = userAIConfigService.openCustomApi(userProductSet);
                boolean canTrial = userTrialMapper.canTrial(userId, ProductTypeEnum.AI_SEAT.getCode(), appBizConfig);
                if (!existAiSeat && !canTrial && !openCustomApi) {
                    openAiSeat = false;
                }
            }
            userInfoVO.setAiSeatStatus(openAiSeat);
        }
        return userInfoVO;
    }

    public void savePreference(UserInfoVO userInfoVO) {
        Long userId = HeaderContext.getHeader().getUserId();
        LambdaUpdateWrapper<UserInfoDO> condition = new LambdaUpdateWrapper<>();
        condition.eq(UserInfoDO::getId, userId);
        if (Objects.nonNull(userInfoVO.getPreference())) {
            condition.set(UserInfoDO::getPreference, JSONUtil.toJsonStr(userInfoVO.getPreference()));
        }
        if (StringUtils.isNotBlank(userInfoVO.getEmail())) {
            condition.set(UserInfoDO::getEmail, userInfoVO.getEmail());
        }
        if (StringUtils.isNotBlank(userInfoVO.getPhone())) {
            condition.set(UserInfoDO::getPhone, userInfoVO.getPhone());
        }

        // 如果尝试开启AI坐席，则判断是否有在有效期内的AI坐席产品
        if (Objects.equals(userInfoVO.getAiSeatStatus(), AiSeatStatusEnum.OPEN.getBool())) {
            Set<Integer> productTypeSet = productService.queryUserValidAllProductType(userId);
            Integer productType = ProductTypeEnum.AI_SEAT.getCode();
            String userIdAndProductTypeKey = userId + "-" + productType;
            if (!(productTypeSet.contains(productType) || userAIConfigService.openCustomApi(productTypeSet))) {
                // 检查用户是否可以试用
                if (!userTrialMapper.canTrial(userId, productType, appBizConfig)) {
                    if (productNotAuthorizedCache.contains(userIdAndProductTypeKey)) {
                        productNotAuthorizedCache.set(userIdAndProductTypeKey, true);
                        throw new ApplicationException("不存在AI坐席且试用结束；请点击【AI坐席】购买后重试");
                    } else {
                        productNotAuthorizedCache.set(userIdAndProductTypeKey, true);
                        throw new ApplicationException("不存在AI坐席且试用结束；请点击【AI坐席】购买后重试", BizCodeEnum.PRODUCT_NOT_AUTHORIZED.getCode());
                    }
                }
                HeaderContext.getHeader().setRespMsg("AI坐席试用已开启");
            }
        }
        condition.set(UserInfoDO::getAiSeatStatus, userInfoVO.getAiSeatStatus());
        // 必须要填入实体对象，mybatisPlus才能自动填充（set的字段还是按照condition中set的字段来更新，不用担心实体对象为空更新进去）
        userInfoMapper.update(new UserInfoDO(), condition);
    }


    private static @NotNull UserInfoVO parsePhoneAndEmail(String summarize) {
        // 正则匹配； 数据入库，【手机号，邮箱，简历文本，标签，意向】
        Matcher phoneMatcher = RePatternConstant.PHONE_PATTERN.matcher(summarize);
        Matcher emailMatcher = RePatternConstant.EMAIL_PATTERN.matcher(summarize);

        String phone = "";
        if (phoneMatcher.find()) {
            phone = phoneMatcher.group(1);
        }

        String email = "";
        if (emailMatcher.find()) {
            email = emailMatcher.group(1);
        }

        if (StringUtils.isBlank(phone) || StringUtils.isBlank(email)) {
            log.warn("简历正则匹配失败 phone:{} email:{} resume:{}", phone, email, summarize);
        }

        return new UserInfoVO(phone, email);
    }
}
