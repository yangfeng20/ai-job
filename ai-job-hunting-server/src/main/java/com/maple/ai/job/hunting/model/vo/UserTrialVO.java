package com.maple.ai.job.hunting.model.vo;

import com.maple.ai.job.hunting.model.common.BaseVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserTrialVO extends BaseVO {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 产品类型(产品能力)
     */
    private Integer productType;

    /**
     * 试用次数
     */
    private Integer trialCount;

    /**
     * 描述备注
     */
    private String desc;
}