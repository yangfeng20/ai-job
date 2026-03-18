package com.maple.ai.job.hunting.service.biz;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.DesensitizedUtil;
import com.maple.ai.job.hunting.config.AppBizConfig;
import com.maple.ai.job.hunting.emums.ProductEnum;
import com.maple.ai.job.hunting.emums.ProductTypeEnum;
import com.maple.ai.job.hunting.emums.UserInvitesStatusEnum;
import com.maple.ai.job.hunting.frame.exp.ApplicationException;
import com.maple.ai.job.hunting.mapper.UserInfoMapper;
import com.maple.ai.job.hunting.mapper.UserInvitesMapper;
import com.maple.ai.job.hunting.mapper.UserProductMapper;
import com.maple.ai.job.hunting.mapper.UserTrialMapper;
import com.maple.ai.job.hunting.model.bo.UserInfoDO;
import com.maple.ai.job.hunting.model.bo.UserInvitesDO;
import com.maple.ai.job.hunting.model.bo.UserProductDO;
import com.maple.ai.job.hunting.model.bo.UserTrialDO;
import com.maple.ai.job.hunting.model.vo.UserInvitesVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * 用户邀请服务
 *
 * @author gaoping
 * @since 2025/03/23
 */
@Slf4j
@Service
public class UserInvitesService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserInvitesMapper userInvitesMapper;

    @Resource
    private UserTrialMapper userTrialMapper;

    @Resource
    private UserProductMapper userProductMapper;

    @Resource
    private AppBizConfig appBizConfig;

    @Transactional(rollbackFor = Throwable.class)
    public void bindInviteCode(String inviteCode, String name, Long userId) {
        UserInfoDO userInfoDO = userInfoMapper.selectByInviteCode(inviteCode);
        if (userInfoDO == null) {
            throw new ApplicationException("邀请码不存在");
        }

        UserInfoDO currentUser = userInfoMapper.selectById(userId);
        if (Objects.equals(currentUser.getInviteCode(), inviteCode)) {
            throw new ApplicationException("无法绑定自己的邀请码");
        }
        if (StringUtils.isNotBlank(currentUser.getBindInviteCode())) {
            throw new ApplicationException("已绑定邀请码；无法重复绑定");
        }
        currentUser.setBindInviteCode(inviteCode);

        UserInvitesDO userInvitesDO = new UserInvitesDO();
        userInvitesDO.setInviteCode(inviteCode);
        userInvitesDO.setBeInviteeUserId(userId);
        userInvitesDO.setToInviterUserId(userInfoDO.getId());
        userInvitesDO.setStatus(UserInvitesStatusEnum.NORMAL.getCode());
        userInvitesDO.setBeInviteeUsername(name);

        UserTrialDO inviteeUserTrialDO = new UserTrialDO(userId, ProductTypeEnum.AI_SEAT.getCode(),
                buildAiSeatInitTrialCount(20), "绑定邀请码赠送");

        try {
            userInvitesMapper.insert(userInvitesDO);
        } catch (DuplicateKeyException e) {
            throw new ApplicationException("邀请码重复绑定");
        }
        userTrialMapper.insert(inviteeUserTrialDO);
        userInfoMapper.updateById(currentUser);
    }

    public List<UserInvitesVO> userInviteList(Long userId) {
        List<UserInvitesDO> doList = userInvitesMapper.queryBeInviteeList(userId);
        List<UserInvitesVO> userInvitesVoList = doList.stream().map(userInvitesDO -> BeanUtil.copyProperties(userInvitesDO, UserInvitesVO.class)).toList();
        userInvitesVoList.forEach(userInvitesVo -> {
            String beInviteeUsername = userInvitesVo.getBeInviteeUsername();
            if (StringUtils.isNotBlank(beInviteeUsername)) {
                userInvitesVo.setBeInviteeUsername(DesensitizedUtil.chineseName(beInviteeUsername));
            }

        });
        return userInvitesVoList;
    }

    public void exchangeProduct(Integer productId, Long userId) {
        List<UserInvitesDO> unArchivedInvitesList = userInvitesMapper.queryBeInviteeList(userId, UserInvitesStatusEnum.NORMAL.getCode());
        int count = unArchivedInvitesList.size();
        if (count < 1) {
            throw new ApplicationException("邀请用户不足，无法兑换对应产品");
        }
        if (productId == -1) {
            UserTrialDO ToInviterUserTrialDO = new UserTrialDO(userId, ProductTypeEnum.AI_SEAT.getCode(),
                    buildAiSeatInitTrialCount(50), "邀请用户赠送");
            userTrialMapper.insert(ToInviterUserTrialDO);
            // 被邀请人状态归档
            archive(unArchivedInvitesList, 1);
            return;
        }

        if (Arrays.stream(ProductEnum.values()).noneMatch(productEnum -> Objects.equals(productEnum.getCode(), productId))) {
            throw new ApplicationException("产品不存在");
        }

        UserProductDO userProductDO = buildUserProduct(productId, userId);
        boolean isWeek = ProductEnum.BASE.getCode().equals(productId);
        archive(unArchivedInvitesList, isWeek ? 4 : 2);
        userProductMapper.insert(userProductDO);
    }

    private void archive(List<UserInvitesDO> unArchivedInvitesList, int count) {
        for (int i = 0; i < count; i++) {
            UserInvitesDO userInvitesDO = unArchivedInvitesList.get(i);
            userInvitesDO.setStatus(UserInvitesStatusEnum.ARCHIVE.getCode());
            userInvitesMapper.updateById(userInvitesDO);
        }
    }

    private @NotNull UserProductDO buildUserProduct(Integer productId, Long userId) {
        UserProductDO userProductDO = new UserProductDO();
        userProductDO.setUserId(userId);
        ProductEnum productEnum = ProductEnum.getByCode(productId);

        // 查询历史产品，累计产品时间
        List<UserProductDO> userProductDOList = userProductMapper.queryUserValidAllProduct(userId);
        Date historyProductMaxValidityEndTime = userProductDOList.stream().filter(product -> product.getProductId().equals(productId.longValue()))
                .max(Comparator.comparing(UserProductDO::getPeriodOfValidityEndTime))
                .map(UserProductDO::getPeriodOfValidityEndTime).orElse(new Date());

        userProductDO.setOrderId(-1L);
        userProductDO.setProductType(productEnum.getProductTypes().toString().trim());
        userProductDO.setProductId(productId.longValue());
        // 累积同类产品的有效期时间
        userProductDO.setPeriodOfValidityStartTime(historyProductMaxValidityEndTime);
        userProductDO.setPeriodOfValidityEndTime(DateUtil.offsetDay(historyProductMaxValidityEndTime, productEnum.getDaysOfValidity()));
        return userProductDO;
    }

    public Integer buildAiSeatInitTrialCount(Integer freeCount) {
        Integer maxLimit = appBizConfig.getProductTrialCountMap().get(ProductTypeEnum.AI_SEAT.getCode());
        return maxLimit - freeCount;
    }
}
