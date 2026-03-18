package com.maple.ai.job.hunting.frame.filter;

import com.maple.ai.job.hunting.frame.exp.ApplicationException;
import com.maple.smart.config.core.annotation.JsonValue;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 过滤器，用于限制/test/路径下的所有请求必须来自本地主机。
 *
 * @author Gemini
 * @since 2025/11/28
 */
@Component
@Order(5)
public class TestControllerFilter implements Filter {

    @JsonValue("${allowed.ips:[\"127.0.0.1\",\"0:0:0:0:0:0:0:1\"]}")
    private Set<String> ALLOWED_IPS = new HashSet<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();

        // 仅对/test/路径下的请求应用此过滤器
        if (requestURI.startsWith("/test/")) {
            String remoteAddr = httpRequest.getRemoteAddr();

            if (ALLOWED_IPS.contains(remoteAddr)) {
                // IP地址在允许列表中，继续处理请求
                chain.doFilter(request, response);
            } else {
                // IP地址不在允许列表中，拒绝访问
                throw new ApplicationException("访问拒绝");
            }
        } else {
            // 对于其他路径的请求，不进行干预，直接放行
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 无需初始化操作
    }

    @Override
    public void destroy() {
        // 无需销毁操作
    }
}
