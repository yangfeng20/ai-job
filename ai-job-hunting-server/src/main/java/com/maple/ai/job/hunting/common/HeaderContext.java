package com.maple.ai.job.hunting.common;

import cn.hutool.core.util.IdUtil;
import com.maple.ai.job.hunting.model.vo.UserInfoVO;

import java.util.Objects;

/**
 * @author 杨锋
 * @since 2022/6/25 21:11
 * desc:
 */

public class HeaderContext {

    private final static ThreadLocal<Header> THREAD_LOCAL = new ThreadLocal<>();

    private final static Header DEFAULT_HEADER = new Header();


    public static Header getHeader() {
        Header header = THREAD_LOCAL.get();

        return Objects.isNull(header) ? DEFAULT_HEADER : header;
    }


    public static void initHeader(UserInfoVO UserInfoVO, String ip, String requestUri) {
        String uuid = IdUtil.simpleUUID();
        THREAD_LOCAL.set(new Header(UserInfoVO, uuid, requestUri, null, false, ip, null));
    }

    public static void initHeader(UserInfoVO UserInfoVO, boolean isAdmin, String ip, String requestUri) {
        String uuid = IdUtil.simpleUUID();
        THREAD_LOCAL.set(new Header(UserInfoVO, uuid, requestUri, null, isAdmin, ip, null));
    }


    public static void setHeader(Header header) {
        THREAD_LOCAL.set(header);
    }

    public static void clear() {
        THREAD_LOCAL.remove();
    }
}
