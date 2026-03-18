package com.maple.ai.job.hunting.common;

import com.maple.ai.job.hunting.model.vo.UserInfoVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

/**
 * @author yangfeng
 * @since : 2023/11/30 14:48
 * desc:
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Header {

    
    private UserInfoVO userInfoVO;

    
    private String requestId;

    
    private String requestUri;

    /**
     * 扩展信息
     */
    
    private Map<String, Object> extend;

    /**
     * 是否为管理员
     */
    private boolean admin;

    
    private String ip;

    private String respMsg;



    public Long getUserId(){
        return Optional.ofNullable(userInfoVO).map(UserInfoVO::getId).orElse(-1L);
    }
}
