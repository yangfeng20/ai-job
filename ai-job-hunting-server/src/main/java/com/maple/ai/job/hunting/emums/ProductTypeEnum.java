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
public enum ProductTypeEnum {

    /**
     *
     */
    NULL,
    AI_SEAT(1, "ai坐席"),
    AI_ASSISTANT(2, "ai助理(副驾驶)"),
    BATCH_PUSH(3, "批量投递"),
    SEND_CUSTOM_MSG(4, "自定义招呼语"),
    IMAGE_RESUME(5, "图片简历"),
    AI_CUSTOM_MSG(6, "ai招呼语"),
    CUSTOM_API(7, "自有API"),
    AI_FILTER(8, "ai过滤"),
    MODEL_FINE_TUNING(9, "模型微调"),

    TRY_FRESH_ALL(101, "尝鲜版所有功能"),
    BASE_ALL(102, "普通版所有功能"),
    ;

    private Integer code;

    private String desc;


    public static ProductTypeEnum getByDesc(String desc) {
        for (ProductTypeEnum fileType : ProductTypeEnum.values()) {
            if (fileType.desc != null && fileType.desc.equalsIgnoreCase(desc)) {
                return fileType;
            }
        }

        return ProductTypeEnum.NULL;
    }

    public static ProductTypeEnum getByCode(Integer code) {
        for (ProductTypeEnum fileType : ProductTypeEnum.values()) {
            if (fileType.code != null && fileType.code.equals(code)) {
                return fileType;
            }
        }

        return ProductTypeEnum.NULL;
    }
}
