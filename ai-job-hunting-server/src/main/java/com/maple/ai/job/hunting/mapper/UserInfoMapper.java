package com.maple.ai.job.hunting.mapper;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.ai.job.hunting.model.bo.UserInfoDO;
import com.maple.ai.job.hunting.model.entity.PreferenceEntity;

import java.util.Optional;

/**
 * 用户信息表(UserInfo)表数据库访问层
 *
 * @author makejava
 * @since 2024-05-13 15:49:51
 */
public interface UserInfoMapper extends BaseMapper<UserInfoDO> {

    default UserInfoDO selectByPhone(String phone) {
        return this.selectOne(new LambdaQueryWrapper<UserInfoDO>().eq(UserInfoDO::getPhone, phone));
    }

    default UserInfoDO selectByUniqueId(String uniqueId) {
        return this.selectOne(new LambdaQueryWrapper<UserInfoDO>().eq(UserInfoDO::getUniqueId, uniqueId));
    }

    default UserInfoDO selectByInviteCode(String inviteCode) {
        return this.selectOne(new LambdaQueryWrapper<UserInfoDO>().eq(UserInfoDO::getInviteCode, inviteCode));
    }

    default PreferenceEntity selectPreferenceByUserId(Long userId) {
        return selectPreferenceOptByUserId(userId).orElse(null);
    }

    default Optional<PreferenceEntity> selectPreferenceOptByUserId(Long userId) {
        UserInfoDO userInfoDO = this.selectById(userId);
        return Optional.ofNullable(userInfoDO).map(UserInfoDO::getPreference)
                .filter(JSON::isValidObject)
                .map(preference -> JSON.parseObject(preference, PreferenceEntity.class));
    }

}


