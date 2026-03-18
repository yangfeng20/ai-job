package com.maple.ai.job.hunting.emums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author maple
 * Created Date: 2024/5/10 11:35
 * Description:
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum OrderStatusEnum {

    /**
     *
     */
    NULL,
    PRE_GENERATE(1, "预生成"),
    PAID(2, "已支付"),
    TIMEOUT_CLOSE(3, "超时关闭"),
    // 生成的订单未支付，重新登录了
    NOT_PAY_CLOSE(4, "未支付关闭"),
    ;


    private Integer code;

    private String desc;


    public static OrderStatusEnum getByDesc(String desc) {
        for (OrderStatusEnum fileType : OrderStatusEnum.values()) {
            if (fileType.desc != null && fileType.desc.equalsIgnoreCase(desc)) {
                return fileType;
            }
        }

        return OrderStatusEnum.NULL;
    }

    public static OrderStatusEnum getByCode(Integer code) {
        for (OrderStatusEnum fileType : OrderStatusEnum.values()) {
            if (fileType.code != null && fileType.code.equals(code)) {
                return fileType;
            }
        }

        return OrderStatusEnum.NULL;
    }
}
