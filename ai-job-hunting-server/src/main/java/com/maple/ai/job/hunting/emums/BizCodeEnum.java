package com.maple.ai.job.hunting.emums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author maple
 * Created Date: 2023/12/27 9:33
 * Description:
 */

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum BizCodeEnum {
    /**
     *
     */
    NULL,
    NOT_LOGIN(401, "401 not login"),
    PARAM_ERROR(410, "参数错误"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

    USER_NOT_EXIST(2000, "用户不存在"),
    PROMOTION_CODE_EXPIRED(2001, "该优惠券码已过期或不存在"),
    AI_CONFIG_TEST_FAILED(2002, "AI配置测试失败"),

    // 产品未授权(未购买，已过期，试用结束)
    PRODUCT_NOT_AUTHORIZED(5001, "产品未授权"),
    ;


    private Integer code;

    private String desc;


    public static BizCodeEnum getByDesc(String desc) {
        for (BizCodeEnum fileType : BizCodeEnum.values()) {
            if (fileType.desc != null && fileType.desc.equalsIgnoreCase(desc)) {
                return fileType;
            }
        }

        return BizCodeEnum.NULL;
    }

    public static BizCodeEnum getByCode(Integer code) {
        for (BizCodeEnum fileType : BizCodeEnum.values()) {
            if (fileType.code != null && fileType.code.equals(code)) {
                return fileType;
            }
        }

        return BizCodeEnum.NULL;
    }
}


