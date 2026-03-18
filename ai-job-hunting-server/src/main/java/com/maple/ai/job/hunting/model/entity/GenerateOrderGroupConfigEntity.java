package com.maple.ai.job.hunting.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author maple
 * Created Date: 2024/5/21 20:15
 * Description:
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOrderGroupConfigEntity {

    public static final String DEFAULT_CONFIG = """
        {
          "promotionCodeMap": {
            "08-22-61c19dfb-f55012b302fe": [
              {
                "totalAmount": 1.9,
                "subject": "ai-job",
                "title": "秒杀版",
                "adText": "限时秒杀版：批量投递简历，发送自定义招呼语，展现您的个人优势，ai坐席快速响应，快人一步。（限时限量，原价：5.72）",
                "productId": 1,
                "validityDate": "2025-09-25 00:00:00"
              }
            ]
          },
          "defaultConfigList": [
            {
              "totalAmount": 5.72,
              "subject": "ai-job",
              "title": "尝鲜版",
              "adText": "极速尝鲜版：批量投递简历，发送自定义招呼语，图片简历，ai坐席快速响应，快人一步。",
              "productId": 1
            },
            {
              "totalAmount": 29.1,
              "subject": "ai-job",
              "title": "基础版",
              "adText": "高速基础版：尝鲜版所有功能，ai招呼语，ai过滤，高意向通知",
              "productId": 2
            },
            {
              "totalAmount": 49.1,
              "subject": "ai-job",
              "title": "plus版",
              "adText": "plus版：高级版所有功能，ai招呼语，ai过滤，大模型微调，高意向通知",
              "productId": 3
            },
            {
              "totalAmount": 2,
              "subject": "ai-job",
              "title": "API",
              "adText": "API版：使用自有api key用于ai坐席；成本维护，boss能力交互",
              "productId": 4
            },
            {
              "totalAmount": 4.1,
              "subject": "ai-job",
              "title": "API扩展",
              "adText": "API扩展包：结合自有API可使用ai招呼语，ai过滤，大模型微调等能力",
              "productId": 5
            }
          ]
        }
        """;

    private Map<String, List<GenerateOrderConfigEntity>> promotionCodeMap = Collections.emptyMap();

    private List<GenerateOrderConfigEntity> defaultConfigList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateOrderConfigEntity {

        private float totalAmount;

        private String subject;

        private String title;

        private String adText;

        private Integer productId;

        private Date validityDate;
    }
}
