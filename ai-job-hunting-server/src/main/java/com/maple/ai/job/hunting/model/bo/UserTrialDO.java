package com.maple.ai.job.hunting.model.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.maple.ai.job.hunting.model.common.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author maple
 * Created Date: 2024/6/1 10:00
 * Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("user_trial")
public class UserTrialDO extends BaseDO {

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
     * 这里为剩余次数
     */
    private Integer trialCount;

    /**
     * 描述备注
     */
    @TableField(value = "`desc`")
    private String desc;
}