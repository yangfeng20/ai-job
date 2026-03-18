package com.maple.ai.job.hunting.mapper;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.ai.job.hunting.emums.OrderStatusEnum;
import com.maple.ai.job.hunting.entity.OrderDO;

/**
 * (Order)表数据库访问层
 *
 * @author maple
 * @since 2024-05-16 23:46:16
 */
public interface OrderMapper extends BaseMapper<OrderDO> {


    default void updateOrderStatus(Long orderId, OrderStatusEnum orderStatus) {
        if (orderId == null || orderStatus == null) {
            throw new IllegalArgumentException("orderId or orderStatus is null");
        }
        update(new OrderDO(), new LambdaUpdateWrapper<OrderDO>()
                .eq(OrderDO::getId, orderId)
                .set(OrderDO::getStatus, orderStatus.getCode()));
    }


}


