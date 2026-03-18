package com.maple.ai.job.hunting.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maple.ai.job.hunting.model.common.BaseVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author maple
 * Created Date: 2024/6/5 17:17
 * Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductVO extends BaseVO {

    private String productName;

    /**
     * 产品能力列表
     */
    private List<String> powerList;

    /**
     * 有效期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date periodOfValidityStartTime;

    /**
     * 有效期结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date periodOfValidityEndTime;
}
