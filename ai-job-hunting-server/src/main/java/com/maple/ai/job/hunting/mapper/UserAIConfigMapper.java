package com.maple.ai.job.hunting.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.ai.job.hunting.model.bo.UserAIConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户AI配置Mapper
 *
 * @author gaoping
 * @since 2025/04/11
 */
public interface UserAIConfigMapper extends BaseMapper<UserAIConfigDO> {

    /**
     * 获取用户启用的有效配置
     */
    default UserAIConfigDO getEnabledConfig(Long userId) {
        return this.selectOne(new LambdaQueryWrapper<UserAIConfigDO>()
                .eq(UserAIConfigDO::getUserId, userId)
                .eq(UserAIConfigDO::getStatus, 1)
                .eq(UserAIConfigDO::getTestPassed, 1)
                .orderByDesc(UserAIConfigDO::getId)
                .last("LIMIT 1"));
    }
    /**
     * 获取用户启用的有效配置
     */
    default UserAIConfigDO selectByUserId(Long userId) {
        return this.selectOne(new LambdaQueryWrapper<UserAIConfigDO>()
                .eq(UserAIConfigDO::getUserId, userId)
                .orderByDesc(UserAIConfigDO::getId)
                .last("LIMIT 1"));
    }

    /**
     * 获取用户最新配置（不考虑状态）
     */
    default UserAIConfigDO getLatestConfig(Long userId) {
        return this.selectOne(new LambdaQueryWrapper<UserAIConfigDO>()
                .eq(UserAIConfigDO::getUserId, userId)
                .orderByDesc(UserAIConfigDO::getId)
                .last("LIMIT 1"));
    }

}
