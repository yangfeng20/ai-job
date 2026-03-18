package com.maple.ai.job.hunting.emums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.maple.ai.job.hunting.emums.ProductTypeEnum.*;

/**
 * @author maple
 * Created Date: 2024/5/10 11:35
 * Description:
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ProductEnum {

    /**
     *
     */
    NULL,
    TRY_FRESH(1, "尝鲜版", 1, new ProductTypeEnum[]{BATCH_PUSH, SEND_CUSTOM_MSG, AI_SEAT, IMAGE_RESUME}),
    BASE(2, "普通版", 7, new ProductTypeEnum[]{TRY_FRESH_ALL, AI_SEAT, AI_CUSTOM_MSG, AI_FILTER}),
    PLUS(3, "plus版", 15, new ProductTypeEnum[]{BASE_ALL, AI_SEAT, AI_CUSTOM_MSG, AI_FILTER, MODEL_FINE_TUNING, CUSTOM_API}),
    API(4, "自有API", 15, new ProductTypeEnum[]{CUSTOM_API,}),
    API_EXT(5, "API扩展包", 7, new ProductTypeEnum[]{AI_CUSTOM_MSG, AI_FILTER, MODEL_FINE_TUNING}),
    ;


    private Integer code;

    private String desc;

    /**
     * 有效天数
     */
    private Integer daysOfValidity;

    private ProductTypeEnum[] productTypeEnums;

    public static ProductEnum getByDesc(String desc) {
        for (ProductEnum fileType : ProductEnum.values()) {
            if (fileType.desc != null && fileType.desc.equalsIgnoreCase(desc)) {
                return fileType;
            }
        }

        return ProductEnum.NULL;
    }

    public static ProductEnum getByCode(Integer code) {
        for (ProductEnum fileType : ProductEnum.values()) {
            if (fileType.code != null && fileType.code.equals(code)) {
                return fileType;
            }
        }

        return ProductEnum.NULL;
    }

    public List<Integer> getProductTypes() {
        return Arrays.stream(productTypeEnums).map(ProductTypeEnum::getCode).collect(Collectors.toList());
    }

    public List<String> getProductTypeDescList() {
        return Arrays.stream(productTypeEnums).map(ProductTypeEnum::getDesc).collect(Collectors.toList());
    }
}
