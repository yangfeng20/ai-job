package com.maple.ai.job.hunting.model.bo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.maple.ai.job.hunting.model.common.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author maple
 * Created Date: 2024/5/24 17:26
 * Description:
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("user_product")
public class UserProductDO extends BaseDO {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 产品类型(产品能力)
     */
    private String productType;

    /**
     * 有效期开始时间
     */
    private Date periodOfValidityStartTime;

    /**
     * 有效期结束时间
     */
    private Date periodOfValidityEndTime;
}
