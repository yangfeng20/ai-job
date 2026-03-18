package com.maple.ai.job.hunting;


import com.maple.smart.config.core.annotation.EnableSmartConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author maple
 * @since 2024/3/13 22:00
 * Description:
 */

@EnableSmartConfig(webUiPort = 6768, descInfer = true, defaultValEcho = true)
@MapperScan("com.maple.ai.job.hunting.mapper")
@EnableAspectJAutoProxy
@SpringBootApplication
public class AiJobHuntingApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiJobHuntingApplication.class, args);
    }


}
