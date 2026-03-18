package com.maple.ai.job.hunting.model.vo;

import com.maple.ai.job.hunting.emums.BossOperationTypeEnum;
import com.maple.ai.job.hunting.emums.JobSeekerClonedAnswerTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * @author maple
 * Created Date: 2024/5/14 14:41
 * Description:
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerClonedResultVO {

    /**
     * 回复类型
     * @see com.maple.ai.job.hunting.emums.JobSeekerClonedAnswerTypeEnum
     */
    private List<Integer> answerTypeList;

    /**
     * 回复内容
     */
    private String answerContent;

    /**
     * 操作类型
     * @see BossOperationTypeEnum
     */
    private List<Integer> operationTypeList;

    public JobSeekerClonedResultVO(List<Integer> answerTypeList, String answerContent) {
        this.answerTypeList = answerTypeList;
        this.answerContent = answerContent;
    }

    public JobSeekerClonedResultVO(JobSeekerClonedAnswerTypeEnum answerType, String answerContent) {
        this.answerTypeList = Collections.singletonList(answerType.getCode());
        this.answerContent = answerContent;
    }
}
