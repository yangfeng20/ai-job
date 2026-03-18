package com.maple.ai.job.hunting.model;

import com.maple.ai.job.hunting.emums.JobSeekerClonedAnswerTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maple
 * Created Date: 2024/5/8 11:50
 * Description:
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionResult {

    private String sessionId;

    private String answer;

    private Object extra;

    private JobSeekerClonedAnswerTypeEnum answerStatus;


    public ChatSessionResult(String sessionId, String answer) {
        this.sessionId = sessionId;
        this.answer = answer;
    }
}
