package com.maple.ai.job.hunting.model.common;


import lombok.Data;

/**
 * @author 杨锋
 * @date 2022/6/25 15:19
 * desc:
 * ResponseList
 */


@Data
public class RequestList {

    /**
     * 当前页
     */
    
    private Integer page = 1;

    /**
     * 每页大小
     */
    
    private Integer size = 9;

    /**
     * 排序
     */
    
    private Sort sort;
}
