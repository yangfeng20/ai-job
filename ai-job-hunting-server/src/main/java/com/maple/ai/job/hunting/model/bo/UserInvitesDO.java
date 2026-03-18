package com.maple.ai.job.hunting.model.bo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.maple.ai.job.hunting.model.common.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * 用户邀请 Do
 *
 * @author gaoping
 * @since 2025/03/23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_invites")
@EqualsAndHashCode(callSuper = true)
public class UserInvitesDO extends BaseDO {
    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 邀请人id
     */
    private Long toInviterUserId;

    /**
     * 被邀请人id
     */
    private Long beInviteeUserId;

    /**
     * 被邀请人用户名
     */
    private String beInviteeUsername;

    /**
     * 状态 1:正常 2:归档
     */
    private Integer status;

}
