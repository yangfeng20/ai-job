package com.maple.ai.job.hunting.frame.filter;

import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.config.AppBizConfig;
import com.maple.ai.job.hunting.emums.BizCodeEnum;
import com.maple.ai.job.hunting.frame.cache.SysSessionCache;
import com.maple.ai.job.hunting.frame.exp.ApplicationException;
import com.maple.ai.job.hunting.model.vo.UserInfoVO;
import com.maple.smart.config.core.annotation.JsonValue;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import jakarta.annotation.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @author yangfeng
 * @since : 2023/11/29 17:36
 * desc:
 */

@Order(3)
@Component
public class LoginFilter implements Filter {

    @Resource
    private SysSessionCache sysSessionCache;

    @Resource
    private AppBizConfig appBizConfig;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @JsonValue("${login-Filter.freeLoginArray:[]}")
    private List<String> freeLoginList;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String ipAddress = getIpAddress(request);
        String requestUri = request.getRequestURI();
        // 免登地址
        if (freeLoginUri(requestUri)) {
            HeaderContext.initHeader(null, ipAddress, requestUri);
            try {
                filterChain.doFilter(servletRequest, servletResponse);
            } finally {
                HeaderContext.clear();
            }
            return;
        }

        String authorization = Optional.ofNullable(request.getHeader("Authorization")).orElse("");
        if (StringUtils.isBlank(authorization) || !sysSessionCache.contains(authorization)) {
            throw new ApplicationException(BizCodeEnum.NOT_LOGIN);
        }

        UserInfoVO userInfoVO = sysSessionCache.get(authorization);
        HeaderContext.initHeader(userInfoVO, appBizConfig.getAdminUserIdList().contains(userInfoVO.getId()), ipAddress, requestUri);
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            HeaderContext.clear();
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    public String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }


    private boolean freeLoginUri(String uri) {
        for (String configUri : freeLoginList) {
            if (antPathMatcher.match(configUri, uri)) {
                return true;
            }
        }
        return false;
    }
}
