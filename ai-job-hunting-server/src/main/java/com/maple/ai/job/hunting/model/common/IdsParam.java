package com.maple.ai.job.hunting.model.common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 杨锋
 * @date 2022/6/25 19:15
 * desc:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor

public class IdsParam {

    
    private List<Long> ids;
}
