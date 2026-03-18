package com.maple.ai.job.hunting.model.param;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * aiFilter 参数
 *
 * @author gaoping
 * @since 2025/06/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIFilterParam {

    @NotEmpty(message = "ai过滤提示词不能为空")
    private String prompt;

    private String jobBaseInfo;

    private String jobExtInfo;
}
