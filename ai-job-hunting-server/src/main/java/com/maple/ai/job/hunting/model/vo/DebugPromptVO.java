package com.maple.ai.job.hunting.model.vo;

import com.maple.ai.job.hunting.model.openai.OpenaiMessage;
import com.maple.ai.job.hunting.model.param.JobSeekerClonedParam;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


/**
 * 调试提示 vo
 *
 * @author gaoping
 * @since 2025/08/21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DebugPromptVO extends JobSeekerClonedParam {

    @Size(max = 5000, message = "提示词不能超过5000个字符")
    private String userPrompt;

    private List<OpenaiMessage> messageList;
}
