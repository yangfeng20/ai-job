package com.maple.ai.job.hunting.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

/**
 * 发送电子邮件实体
 *
 * @author maple
 * @since 2024/06/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailEntity {

    /**
     * 主题
     */
    private String subject;

    /**
     * 发送电子邮件集合
     */
    private Set<String> toSendEmailSet;

    /**
     * 发送用户集合
     */
    private Set<Long> toSendUserSet;

    /**
     * 模板名
     */
    private String templateName;

    /**
     * 参数映射
     */
    private Map<String, Object> paramMap;
}
