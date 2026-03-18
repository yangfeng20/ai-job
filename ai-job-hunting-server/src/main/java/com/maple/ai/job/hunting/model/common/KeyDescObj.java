package com.maple.ai.job.hunting.model.common;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yangfeng
 * @date : 2023/4/4 20:03
 * desc:
 */


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyDescObj {

    
    private Object key;

    
    private Object desc;
}
