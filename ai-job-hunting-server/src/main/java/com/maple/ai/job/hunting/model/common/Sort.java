package com.maple.ai.job.hunting.model.common;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 杨锋
 * @date 2022/6/25 15:22
 * desc: Sort
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Sort {

    /**
     * 排序字段名
     */
    
    private String name;

    /**
     * 排序规则
     */
    
    private String ordering;
}
