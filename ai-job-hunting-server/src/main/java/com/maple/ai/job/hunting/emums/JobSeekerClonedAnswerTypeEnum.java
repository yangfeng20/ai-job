package com.maple.ai.job.hunting.emums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author maple
 * Created Date: 2024/5/10 11:35
 * Description:
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum JobSeekerClonedAnswerTypeEnum {

    /**
     *
     */
    NULL,
    MSG_TEXT(1, "普通文本消息"),
    BOSS_OPERATION(2, "boss操作"),
    STOP(3, "会话停止"),
    AI_SERVICE_EXCEPTION(4, "AI服务异常"),
    ;


    private Integer code;

    private String desc;


    public static JobSeekerClonedAnswerTypeEnum getByDesc(String desc) {
        for (JobSeekerClonedAnswerTypeEnum fileType : JobSeekerClonedAnswerTypeEnum.values()) {
            if (fileType.desc != null && fileType.desc.equalsIgnoreCase(desc)) {
                return fileType;
            }
        }

        return JobSeekerClonedAnswerTypeEnum.NULL;
    }

    public static JobSeekerClonedAnswerTypeEnum getByCode(Integer code) {
        for (JobSeekerClonedAnswerTypeEnum fileType : JobSeekerClonedAnswerTypeEnum.values()) {
            if (fileType.code != null && fileType.code.equals(code)) {
                return fileType;
            }
        }

        return JobSeekerClonedAnswerTypeEnum.NULL;
    }
}
