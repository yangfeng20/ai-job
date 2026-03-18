package com.maple.ai.job.hunting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.maple.ai.job.hunting.model.common.UnIdBaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * (Order)表实体类
 *
 * @author maple
 * @since 2024-05-16 23:46:16
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("`order`")
@EqualsAndHashCode(callSuper = true)
public class OrderDO extends UnIdBaseDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 类型(及产品)
     */
    private Integer type;

    /**
     * 价格
     */
    private BigDecimal price;

}


