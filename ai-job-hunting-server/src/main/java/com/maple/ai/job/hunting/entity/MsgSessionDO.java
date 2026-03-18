package com.maple.ai.job.hunting.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.maple.ai.job.hunting.model.common.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息会话表(MsgSession)表实体类
 *
 * @author maple
 * @since 2024-05-15 09:55:41
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("msg_session")
public class MsgSessionDO extends BaseDO {

    /**
     * 消息上下文
     */
    private String msgContext;

    /**
     * ai类型
     */
    private Integer aiType;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 会话key(用于job唯一键)
     */
    private String sessionKey;

}


