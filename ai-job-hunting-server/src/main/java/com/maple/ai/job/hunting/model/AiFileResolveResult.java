package com.maple.ai.job.hunting.model;

import com.maple.ai.job.hunting.emums.AiFileResolveResultTypeEnum;
import lombok.Builder;
import lombok.Data;

/**
 * @author maple
 * Created Date: 2024/5/10 11:31
 * Description:
 */

@Data
@Builder
public class AiFileResolveResult {

    private String fileId;

    private String originalFileContent;

    private AiFileResolveResultTypeEnum resolveResultType;

    private Object resolveResult;

    private Object extra;

}
