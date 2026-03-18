package com.maple.ai.job.hunting.frame.cache;

import com.maple.ai.job.hunting.model.vo.UserInfoVO;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author yangfeng
 * @date : 2023/11/29 17:25
 * desc:
 */

@Component
public class ProductNotAuthorizedCache extends AbsMemoryCache<String, Boolean> {
    public ProductNotAuthorizedCache() {
        super(2, TimeUnit.HOURS, true);
    }
}
