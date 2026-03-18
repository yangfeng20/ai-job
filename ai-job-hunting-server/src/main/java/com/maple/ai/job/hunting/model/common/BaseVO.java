package com.maple.ai.job.hunting.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maple.ai.job.hunting.model.vo.UserInfoVO;
import lombok.Data;

import java.util.Date;

/**
 * @author 杨锋
 * @date 2022/6/26 14:16
 * desc:
 */

@Data
public class BaseVO {
    /**
     * 创建人
     */

    private UserInfoVO createdUser;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;

    /**
     * 更新人
     */
    private UserInfoVO updatedUser;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedDate;

}
