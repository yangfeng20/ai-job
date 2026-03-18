package com.maple.ai.job.hunting.mapper;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.ai.job.hunting.config.AppBizConfig;
import com.maple.ai.job.hunting.model.bo.UserTrialDO;
import jakarta.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 用户试用映射器
 *
 * @author gaoping
 * @since 2025/03/12
 */
public interface UserTrialMapper extends BaseMapper<UserTrialDO> {


    @Nonnull
    default List<UserTrialDO> getUserTrial(Long userId, Integer productType) {
        LambdaQueryWrapper<UserTrialDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTrialDO::getUserId, userId)
                .eq(UserTrialDO::getProductType, productType);
        List<UserTrialDO> list = selectList(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.singletonList(new UserTrialDO(userId, productType, 0, "免费试用"));
        }
        return list;
    }

    default void incrTrialCount(UserTrialDO userTrialDo) {
        // 没有id则新增，有id则更新
        if (userTrialDo.getId() == null) {
            userTrialDo.setTrialCount(1);
            userTrialDo.setDesc("免费试用");
            insert(userTrialDo);
            return;
        }
        LambdaUpdateWrapper<UserTrialDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserTrialDO::getId, userTrialDo.getId())
                .eq(UserTrialDO::getProductType, userTrialDo.getProductType())
                .setSql("trial_count = trial_count + 1");
        update(new UserTrialDO(), wrapper);
    }

    default boolean canTrial(Long userId, Integer productType, AppBizConfig appBizConfig) {
        List<UserTrialDO> userTrialList = getUserTrial(userId, productType);
        if (CollectionUtils.isEmpty(userTrialList)) {
            return false;
        }

        int maxCount = Optional.ofNullable(appBizConfig.getProductTrialCountMap().get(productType)).orElse(0);

        for (UserTrialDO userTrialDO : userTrialList) {
            int trialCount = Optional.ofNullable(userTrialDO).map(UserTrialDO::getTrialCount).orElse(0);
            if (trialCount < maxCount) {
                return true;
            }
        }

        return false;
    }

    default Pair<Boolean, UserTrialDO> canTrialGet(Long userId, Integer productType, AppBizConfig appBizConfig) {
        List<UserTrialDO> userTrialList = getUserTrial(userId, productType);
        if (CollectionUtils.isEmpty(userTrialList)) {
            return Pair.of(false, null);
        }

        int maxCount = Optional.ofNullable(appBizConfig.getProductTrialCountMap().get(productType)).orElse(0);

        for (UserTrialDO userTrialDO : userTrialList) {
            int trialCount = Optional.ofNullable(userTrialDO).map(UserTrialDO::getTrialCount).orElse(0);
            if (trialCount < maxCount) {
                return Pair.of(true, userTrialDO);
            }
        }

        return Pair.of(false, null);
    }
}
