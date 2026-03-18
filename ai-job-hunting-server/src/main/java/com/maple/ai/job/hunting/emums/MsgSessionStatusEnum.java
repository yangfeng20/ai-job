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
public enum MsgSessionStatusEnum {

    /**
     *
     */
    NULL,
    IN_USE(1, "使用中"),
    /**
     *
     */
    EXPIRED(2, "已过期"),
    ;


    private Integer code;

    private String desc;


    public static MsgSessionStatusEnum getByDesc(String desc) {
        for (MsgSessionStatusEnum fileType : MsgSessionStatusEnum.values()) {
            if (fileType.desc != null && fileType.desc.equalsIgnoreCase(desc)) {
                return fileType;
            }
        }

        return MsgSessionStatusEnum.NULL;
    }

    public static MsgSessionStatusEnum getByCode(Integer code) {
        for (MsgSessionStatusEnum fileType : MsgSessionStatusEnum.values()) {
            if (fileType.code != null && fileType.code.equals(code)) {
                return fileType;
            }
        }

        return MsgSessionStatusEnum.NULL;
    }
}
