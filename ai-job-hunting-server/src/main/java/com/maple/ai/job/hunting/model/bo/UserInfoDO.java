package com.maple.ai.job.hunting.model.bo;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.maple.ai.job.hunting.model.common.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息表(UserInfo)表实体类
 *
 * @author makejava
 * @since 2024-05-13 15:49:51
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_info")
public class UserInfoDO extends BaseDO {

    /**
     * 平台唯一id
     * bossId
     */
    private String uniqueId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮件
     */
    private String email;

    /**
     * 偏好设置
     */
    private String preference;

    /**
     * ai坐席状态 null-未使用 1-使用中(开) 0-使用中(关)
     */
    private Integer aiSeatStatus;

    /**
     * 自己的邀请码
     */
    private String inviteCode;
    /**
     * 绑定的邀请码
     */
    private String bindInviteCode;

    public UserInfoDO(String phone, String email, String uniqueId) {
        this.phone = phone;
        this.email = email;
        this.uniqueId = uniqueId;
        inviteCode = IdUtil.fastSimpleUUID().substring(0, 10).toUpperCase();
    }
}


