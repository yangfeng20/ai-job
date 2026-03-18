package com.maple.ai.job.hunting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * Job Agent 测试员
 *
 * @author gaoping
 * @since 2025/02/27
 */
public class JobAgentTest {

    public static final String[] TEST_QUESTIONS = {
            "fjwoeffjwoeifjiwjfowfjwoi43839j843754%$#$*^&^&^%",
            //"[hi] 你好啊，可以聊一聊~",
            //"助教岗位考虑不",
            //"加我微信",
            //"这边岗位是500强互联网大厂，但目前在招研发岗位都是od形式的。虽然是od形式但是薪资、项目参与权限基本与正编员工无异，且入职培养体系完善，有导师为期半年的带教，项目稳定可参与行业前沿技术研发。面试全程线上，为机考、hr面试、技术面、主管面。",
            //"您好，您目前是还在职，然后也不在深圳哈？",
            //"您好，需要一本及以上本科学历哈",
            //"这个工作的性质请问可以接受吗",
            //"你好这个项目要求现场面试，不知道可以吗？",
            //"不是很合适呢",

    };

    private static final String BASE_URL = "http://localhost:9100";
    private String authToken;

    // 静默登录获取token
    private void silentLogin() throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(BASE_URL + "/api/user/silently/login");
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            post.setEntity(new StringEntity("uniqueId=616475634"));

            HttpResponse response = client.execute(post);
            String responseBody = EntityUtils.toString(response.getEntity());
            authToken = extractAuthToken(responseBody); // 实现JSON解析提取token
        }
    }

    // 执行单轮测试
    private String askQuestion(String question, String jobKey) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(BASE_URL + "/api/job/seeker/cloned/ask");
            post.setHeader("Authorization", authToken);
            post.setHeader("Content-Type", "application/json");


            String jsonBody = String.format("{"
                    + "\"question\": \"%s\","
                    + "\"jobKey\": \"%s\","
                    + "\"jobInfo\": {\"jobTitle\": \"测试岗位\"}"
                    + "}", question, jobKey);

            post.setEntity(new StringEntity(jsonBody, "UTF-8"));
            HttpResponse response = client.execute(post);
            return EntityUtils.toString(response.getEntity());
        }
    }

    @Test
    public void fullTestCycle() throws Exception {
        silentLogin();

        String jobKey = "test-" + UUID.randomUUID();

        for (String question : TEST_QUESTIONS) {
            System.out.println("【测试问题】" + question);
            long startTime = System.currentTimeMillis();

            String response = askQuestion(question, jobKey);
            //System.out.println("【原始响应】" + response);
            System.out.println("【AI响应】" + JSON.parseObject(response).getObject("data", JSONObject.class).getString("answerContent"));

            // 性能指标记录
            long latency = System.currentTimeMillis() - startTime;
            System.out.printf("响应耗时：%dms\n", latency);

            // 建议添加的校验逻辑：
            // 1. 检测是否包含禁用词（符号/表情）
            // 2. 验证响应长度阶梯
            // 3. 检查岗位不匹配时的三要素
            // 4. 无效问题的空响应验证
        }
    }

    private String extractAuthToken(String jsonResponse) {
        // 实现实际的token解析逻辑
        return jsonResponse.split("\"data\":\"")[1].split("\"")[0];
    }
}