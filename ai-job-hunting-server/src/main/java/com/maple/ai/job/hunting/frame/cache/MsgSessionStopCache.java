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
public class MsgSessionStopCache extends AbsMemoryCache<String, Boolean> {
    public MsgSessionStopCache() {
        super(6, TimeUnit.HOURS, false);
    }
}
