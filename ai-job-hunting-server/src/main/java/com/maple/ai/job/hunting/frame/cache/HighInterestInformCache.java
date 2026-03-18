package com.maple.ai.job.hunting.frame.cache;

import com.maple.ai.job.hunting.model.vo.UserInfoVO;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author yangfeng
 * @since : 2023/11/29 17:25
 * desc: 高意向通知缓存
 */

@Component
public class HighInterestInformCache extends AbsMemoryCache<String, Boolean> {
    public HighInterestInformCache() {
        super(1, TimeUnit.HOURS, false);
    }
}
