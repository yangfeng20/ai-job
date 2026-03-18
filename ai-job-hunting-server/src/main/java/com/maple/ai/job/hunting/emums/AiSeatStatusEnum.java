package com.maple.ai.job.hunting.emums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author maple
 * Created Date: 2024/5/10 11:35
 * Description:
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiSeatStatusEnum {

    /**
     *
     */
    NOT_BUY(null, "未购买", null),
    NOT_OPEN(0, "未开启", Boolean.FALSE),
    OPEN(1, "已开启", Boolean.TRUE),
    ;


    private Integer code;

    private String desc;

    private Boolean bool;


    public static AiSeatStatusEnum getByDesc(String desc) {
        for (AiSeatStatusEnum fileType : AiSeatStatusEnum.values()) {
            if (Objects.equals(fileType.desc, desc)) {
                return fileType;
            }
        }

        return AiSeatStatusEnum.NOT_BUY;
    }

    public static AiSeatStatusEnum getByCode(Integer code) {
        for (AiSeatStatusEnum fileType : AiSeatStatusEnum.values()) {
            if (Objects.equals(fileType.code, code)) {
                return fileType;
            }
        }

        return AiSeatStatusEnum.NOT_BUY;
    }
}
