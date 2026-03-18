package com.maple.ai.job.hunting.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.ai.job.hunting.model.bo.UserResumeDO;

/**
 * 用户简历表(UserResume)表数据库访问层
 *
 * @author makejava
 * @since 2024-05-13 15:49:51
 */
public interface UserResumeMapper extends BaseMapper<UserResumeDO> {


    default UserResumeDO selectByUserId(Long userId){
        return this.selectOne(new LambdaQueryWrapper<UserResumeDO>().eq(UserResumeDO::getUserId, userId));
    }

}


