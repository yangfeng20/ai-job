package com.maple.ai.job.hunting.frame.exp;


import com.maple.ai.job.hunting.emums.BizCodeEnum;

/**
 * @author 杨锋
 * @date 2022/6/26 16:01
 * desc:
 */

public class ApplicationException extends RuntimeException {

    private final Integer bizCode;

    public ApplicationException() {
        this.bizCode = BizCodeEnum.INTERNAL_SERVER_ERROR.getCode();
    }

    public ApplicationException(String message) {
        super(message);
        this.bizCode = BizCodeEnum.INTERNAL_SERVER_ERROR.getCode();
    }

    public ApplicationException(BizCodeEnum bizCodeEnum) {
        super(bizCodeEnum.getDesc());
        this.bizCode = bizCodeEnum.getCode();
    }

    public ApplicationException(String message, Integer code) {
        super(message);
        this.bizCode = code;
    }

    public Integer getBizCode() {
        return bizCode;
    }
}
