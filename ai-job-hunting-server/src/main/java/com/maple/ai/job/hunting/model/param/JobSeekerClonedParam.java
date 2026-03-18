package com.maple.ai.job.hunting.model.param;

import com.alibaba.fastjson.JSONObject;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maple
 * Created Date: 2024/5/14 21:28
 * Description:
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerClonedParam {

    @Size(max = 5000, message = "问题超过字符长度限制")
    @NotEmpty(message = "question不能为空")
    private String question;

    /**
     * encryptJobId:bossUid
     */
    @NotEmpty(message = "jobKey不能为空")
    private String jobKey;

    /**
     * 职位信息
     */
    private JSONObject jobInfo;

}
