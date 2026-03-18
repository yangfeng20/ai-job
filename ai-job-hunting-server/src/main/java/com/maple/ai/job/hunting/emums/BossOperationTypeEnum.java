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
public enum BossOperationTypeEnum {

    /**
     *
     */
    NULL,
    SEND_RESUME(1, "发送简历"),
    ;


    private Integer code;

    private String desc;


    public static BossOperationTypeEnum getByDesc(String desc) {
        for (BossOperationTypeEnum fileType : BossOperationTypeEnum.values()) {
            if (fileType.desc != null && fileType.desc.equalsIgnoreCase(desc)) {
                return fileType;
            }
        }

        return BossOperationTypeEnum.NULL;
    }

    public static BossOperationTypeEnum getByCode(Integer code) {
        for (BossOperationTypeEnum fileType : BossOperationTypeEnum.values()) {
            if (fileType.code != null && fileType.code.equals(code)) {
                return fileType;
            }
        }

        return BossOperationTypeEnum.NULL;
    }
}
