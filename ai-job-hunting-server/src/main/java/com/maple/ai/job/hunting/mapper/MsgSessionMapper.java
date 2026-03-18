package com.maple.ai.job.hunting.mapper;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.ai.job.hunting.emums.AITypeEnum;
import com.maple.ai.job.hunting.emums.MsgSessionStatusEnum;
import com.maple.ai.job.hunting.entity.MsgSessionDO;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 消息会话表(MsgSession)表数据库访问层
 *
 * @author makejava
 * @since 2024-05-14 21:09:59
 */
public interface MsgSessionMapper extends BaseMapper<MsgSessionDO> {


    /**
     * 按类型选择使用列表
     * 仅获取最近3天的数据，避免内存爆了
     *
     * @param aiTypeEnum AI 类型枚举
     * @return {@link List}<{@link MsgSessionDO}>
     */
    default List<MsgSessionDO> selectUseListByType(AITypeEnum aiTypeEnum) {
        LambdaQueryWrapper<MsgSessionDO> condition = new LambdaQueryWrapper<>();
        condition.eq(MsgSessionDO::getStatus, MsgSessionStatusEnum.IN_USE.getCode());
        condition.eq(MsgSessionDO::getAiType, aiTypeEnum.getCode());
        // 创建时间为最近3天
        condition.gt(MsgSessionDO::getCreatedDate, DateUtil.offsetDay(new Date(), -3));
        return this.selectList(condition);
    }

    default MsgSessionDO selectBySessionKey(String sessionKey) {
        LambdaQueryWrapper<MsgSessionDO> condition = new LambdaQueryWrapper<>();
        condition.eq(MsgSessionDO::getSessionKey, sessionKey);
        return Optional.ofNullable(this.selectList(condition))
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0)).orElse(null);
    }


    default void updateMsgContextAndAiType(String sessionId, String msgContext, AITypeEnum aiTypeEnum) {
        LambdaUpdateWrapper<MsgSessionDO> condition = new LambdaUpdateWrapper<>();
        condition.eq(MsgSessionDO::getId, sessionId);
        condition.set(MsgSessionDO::getMsgContext, msgContext);
        condition.set(MsgSessionDO::getAiType, aiTypeEnum.getCode());
        this.update(new MsgSessionDO(), condition);
    }

}


