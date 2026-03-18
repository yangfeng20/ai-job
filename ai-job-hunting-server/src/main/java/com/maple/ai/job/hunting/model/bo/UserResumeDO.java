package com.maple.ai.job.hunting.model.bo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.maple.ai.job.hunting.model.common.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用户简历表(UserResume)表实体类
 *
 * @author makejava
 * @since 2024-05-13 15:49:51
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_resume")
@EqualsAndHashCode(callSuper = true)
public class UserResumeDO extends BaseDO {

    public UserResumeDO(Long userId, String resumeContent, String ossFileName, String resumeId) {
        this.userId = userId;
        this.resumeContent = resumeContent;
        this.ossFileName = ossFileName;
        this.resumeId = resumeId;
    }

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 简历原始内容
     */
    private String resumeContent;

    /**
     * oss唯一对象名
     */
    private String ossFileName;

    /**
     * 保存简历url
     */
    private String resumeUrl;

    /**
     * 预设问题
     */
    private String presetProblem;

    /**
     * boss平台的简历id
     **/
    private String resumeId;

}


