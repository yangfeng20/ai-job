package com.maple.ai.job.hunting.controller.test;

import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.config.ai.OpenAIPoolConfig;
import com.maple.ai.job.hunting.model.vo.UserInfoVO;
import com.maple.ai.job.hunting.service.biz.AIAssistantService;
import jakarta.annotation.Resource;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author maple
 * Created Date: 2024/4/22 14:49
 * Description:
 */

@RestController
@RequestMapping("/test")
public class TestController {
    @Resource
    private OpenAiChatClient openAiChatClient;
    @Resource
    private OpenAIPoolConfig.OpenAIPoolManager openAIPoolManager;
    @Resource
    private AIAssistantService aiAssistantService;


    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 100, 10, TimeUnit.HOURS, new ArrayBlockingQueue<>(1000), new ThreadPoolExecutor.CallerRunsPolicy());


    @RequestMapping("00")
    public String test00(String msg) throws Exception {
        if (msg == null || msg.isEmpty()) {
            msg = "你好，你是?";
        }
        System.out.println("开始测试；提问："+msg);
        int poolSize = openAIPoolManager.getPoolSize();
        for (int i = 0; i < poolSize; i++) {
            String finalMsg = msg;
            threadPoolExecutor.submit(() -> {
                OpenAIPoolConfig.OpenAIPoolClient client = null;
                try {
                    client = openAIPoolManager.getClient();
                    String resp = client.call(finalMsg);
                    System.out.println(client.getName() + " 回复 " + resp);
                } catch (Exception e) {
                    System.out.println("------------------------------------------");
                    System.out.println(client.getName() + " 执行失败 " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
        return "ai能力测试中，请查看控制台";
    }

    @RequestMapping("01")
    public String test01(String msg) throws Exception {
        String call = openAIPoolManager.getClient().call(msg);
        System.out.println(call);
        return call;
    }

    @GetMapping("/02")
    public String test02(String msg) throws Exception {
        //AtomicInteger atomicInteger = new AtomicInteger(0);
        //CountDownLatch countDownLatch = new CountDownLatch(1000);
        //for (int i = 0; i < 1000; i++) {
        //    int finalI = i;
        //    threadPoolExecutor.submit(() -> {
        //        try {
        //            String resp = openAiChatClient.call(msg + " " + finalI);
        //            System.out.println(atomicInteger.incrementAndGet() + " 执行结果==>：" + resp);
        //        } catch (Exception e) {
        //            System.out.println("------------------------------------------");
        //            e.printStackTrace();
        //            System.out.println(atomicInteger.get() + " 执行失败");
        //        } finally {
        //            countDownLatch.countDown();
        //        }
        //    });
        //}

        //String resp = openAiChatClient.call(msg + " ");
        System.out.println("等待执行");
        //countDownLatch.await();
        return "success";
    }

    @GetMapping("/04")
    public String test04(String msg) throws Exception {
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setId(1L);
        HeaderContext.getHeader().setUserInfoVO(userInfoVO);
        return aiAssistantService.generateGreeting(1L);
    }
}
