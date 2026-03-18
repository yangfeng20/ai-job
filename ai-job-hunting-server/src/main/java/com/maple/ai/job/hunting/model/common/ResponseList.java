package com.maple.ai.job.hunting.model.common;


import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * @author 杨锋
 * @date 2022/6/25 15:19
 * desc:
 */

@Data

public class ResponseList<T> {

    /**
     * 业务数据
     */
    
    private List<T> content;

    /**
     * 当前页
     */
    
    private Integer page;

    /**
     * 每页大小
     */
    
    private Integer size;

    /**
     * 总页数
     */
    
    private Integer total;

    /**
     * 排序
     */
    
    private Sort sort;


    public ResponseList() {
        this.content = Collections.emptyList();
    }

    public ResponseList(List<T> data, Integer page, Integer size, Integer total, Sort sort) {
        this.content = data;
        this.page = page;
        this.size = size;
        this.total = total;
        this.sort = sort;
    }

    public ResponseList(List<T> data, Integer page, Integer size, Integer total) {
        this.content = data;
        this.page = page;
        this.size = size;
        this.total = total;
    }
}
