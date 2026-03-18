package com.maple.ai.job.hunting.model.common;


import com.maple.ai.job.hunting.common.HeaderContext;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * 响应
 *
 * @author 杨锋
 * @date 2022/6/25 11:39
 * desc:
 */


@Data

public class Response<T> {

    /**
     * 业务数据实体
     */

    private T data;

    /**
     * 状态码
     */

    private Integer code;

    /**
     * 消息
     */

    private String message;

    /**
     * 扩展信息
     */

    private Map<String, Object> extend;

    /**
     * requestId
     */

    private String requestId;


    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("成功");
        response.setData(data);
        response.setRequestId(HeaderContext.getHeader().getRequestId());

        return response;
    }

    public static <T> Response<T> success() {
        Response<T> response = new Response<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("成功");
        response.setRequestId(HeaderContext.getHeader().getRequestId());

        return response;
    }

    private static <T> Response<T> base(T data, Integer code, String message, Map<String, Object> extend) {
        Response<T> response = new Response<>();
        response.setData(data);
        response.setCode(code);
        response.setMessage(message);
        response.setExtend(extend);
        response.setRequestId(HeaderContext.getHeader().getRequestId());
        return response;
    }


    private static <T> Response<T> base(Integer code) {
        Response<T> response = new Response<>();
        response.setCode(code);
        response.setMessage(HttpStatus.valueOf(code).getReasonPhrase());
        response.setRequestId(HeaderContext.getHeader().getRequestId());
        return response;
    }

    private static <T> Response<T> success(T data, Map<String, Object> extend) {
        return base(data, HttpStatus.OK.value(), "成功", extend);
    }

    public static <T> Response<T> success(T data, String message) {
        return base(data, HttpStatus.OK.value(), message, null);
    }

    public static <T> Response<T> nullDataCode(Integer code) {
        HttpStatus httpStatus = HttpStatus.valueOf(code);
        return base(null, httpStatus.value(), httpStatus.getReasonPhrase(), null);
    }

    public static <T> Response<T> error() {
        return base(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    public static <T> Response<T> error(String message) {
        return base(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), message, null);
    }

    public static <T> Response<T> error(Integer code, String message) {
        return base(null, code, message, null);
    }

    public static <T> Response<T> error(String message, Map<String, Object> extend) {
        return base(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), message, extend);
    }
}
