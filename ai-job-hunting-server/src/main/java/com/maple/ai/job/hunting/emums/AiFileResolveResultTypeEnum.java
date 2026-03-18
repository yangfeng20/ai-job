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
public enum AiFileResolveResultTypeEnum {

    /**
     *
     */
    UNRESOLVED(0, "未解析"),
    /**
     *
     */
    MD_JSON(1, "md中的json"),
    ;


    private Integer code;

    private String desc;


    public static AiFileResolveResultTypeEnum getByDesc(String desc) {
        for (AiFileResolveResultTypeEnum fileType : AiFileResolveResultTypeEnum.values()) {
            if (fileType.desc != null && fileType.desc.equalsIgnoreCase(desc)) {
                return fileType;
            }
        }

        return AiFileResolveResultTypeEnum.UNRESOLVED;
    }

    public static AiFileResolveResultTypeEnum getByCode(Integer code) {
        for (AiFileResolveResultTypeEnum fileType : AiFileResolveResultTypeEnum.values()) {
            if (fileType.code != null && fileType.code.equals(code)) {
                return fileType;
            }
        }

        return AiFileResolveResultTypeEnum.UNRESOLVED;
    }
}
