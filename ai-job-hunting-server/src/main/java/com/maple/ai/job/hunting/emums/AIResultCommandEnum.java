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
public enum AIResultCommandEnum {

    /**
     *
     */
    NULL,
    HR_REJECT(1, "COMMAND_HR_REJECT"),
    SEND_RESUME(2, "COMMAND_SEND_RESUME"),
    ;


    private Integer code;

    private String desc;


    public static AIResultCommandEnum getByDesc(String desc) {
        for (AIResultCommandEnum fileType : AIResultCommandEnum.values()) {
            if (fileType.desc != null && fileType.desc.equalsIgnoreCase(desc)) {
                return fileType;
            }
        }

        return AIResultCommandEnum.NULL;
    }

    public static AIResultCommandEnum getByCode(Integer code) {
        for (AIResultCommandEnum fileType : AIResultCommandEnum.values()) {
            if (fileType.code != null && fileType.code.equals(code)) {
                return fileType;
            }
        }

        return AIResultCommandEnum.NULL;
    }
}
