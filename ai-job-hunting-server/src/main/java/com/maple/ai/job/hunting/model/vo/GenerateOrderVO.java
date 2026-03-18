package com.maple.ai.job.hunting.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maple
 * Created Date: 2024/5/15 22:50
 * Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateOrderVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long orderId;

    /**
     * 二维码base64
     * transient 为了接口返回有当前数据。但是在切面日志打印中，忽略当前字段。
     */
    private transient String qrCodeBase64;

    private String title;

    private String desc;

    private Integer validDays;

    private List<String> tags;

    private float totalAmount;

    private Object extra;
}
