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
public enum AITypeEnum {

    /**
     *
     */
    NULL,
    OPENAI(1, "openai"),
    GEMINI(2, "gemini"),
    ;


    private Integer code;

    private String desc;


    public static AITypeEnum getByDesc(String desc) {
        for (AITypeEnum fileType : AITypeEnum.values()) {
            if (fileType.desc != null && fileType.desc.equalsIgnoreCase(desc)) {
                return fileType;
            }
        }

        return AITypeEnum.NULL;
    }

    public static AITypeEnum getByCode(Integer code) {
        for (AITypeEnum fileType : AITypeEnum.values()) {
            if (fileType.code != null && fileType.code.equals(code)) {
                return fileType;
            }
        }

        return AITypeEnum.NULL;
    }
}
