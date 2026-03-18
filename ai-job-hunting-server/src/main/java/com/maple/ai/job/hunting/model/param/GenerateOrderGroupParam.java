package com.maple.ai.job.hunting.model.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maple
 * Created Date: 2024/5/21 20:11
 * Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOrderGroupParam {

    /**
     * 优惠码
     */
    private String promotionCode;
}
