package com.maple.ai.job.hunting.mapper;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.ai.job.hunting.emums.UserInvitesStatusEnum;
import com.maple.ai.job.hunting.model.bo.UserInvitesDO;

import java.util.List;

/**
 * 用户邀请映射器
 *
 * @author makejava
 * @since 2024-05-13 15:49:51
 */
public interface UserInvitesMapper extends BaseMapper<UserInvitesDO> {

    default List<UserInvitesDO> queryBeInviteeList(Long userId) {
        return queryBeInviteeList(userId, null);
    }

    default List<UserInvitesDO> queryBeInviteeList(Long userId, Integer status) {
        LambdaQueryWrapper<UserInvitesDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInvitesDO::getToInviterUserId, userId);
        wrapper.eq(status != null, UserInvitesDO::getStatus, status);
        return selectList(wrapper);
    }


    @SuppressWarnings("unused")
    default Integer countUnArchivedInvites(Long userId) {
        LambdaQueryWrapper<UserInvitesDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInvitesDO::getToInviterUserId, userId)
                .eq(UserInvitesDO::getStatus, UserInvitesStatusEnum.NORMAL.getCode());
        return Math.toIntExact(selectCount(wrapper));
    }

}


