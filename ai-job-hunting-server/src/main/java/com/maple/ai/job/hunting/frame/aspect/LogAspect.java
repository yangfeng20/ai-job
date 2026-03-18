package com.maple.ai.job.hunting.frame.aspect;


import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.frame.GlobalExceptionHandler;
import com.maple.ai.job.hunting.frame.exp.ApplicationException;
import com.maple.ai.job.hunting.model.common.Response;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * 日志切面
 *
 * @author gaoping
 * @since 2025/03/12
 */
@Slf4j
@Aspect
@Order(4)
@Component
public class LogAspect {

    @Resource
    private GlobalExceptionHandler globalExceptionHandler;

    @Pointcut("execution(* com.maple.ai.job.hunting.controller.*.*(..))")
    private void pointcut() {
    }

    @Around("pointcut()")
    public Object logAspect(ProceedingJoinPoint pjp) throws Throwable {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            log.info("request对象为空");
            return pjp.proceed();
        }

        // 请求url
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String requestURI = request.getRequestURI();

        // 请求参数
        Object[] args = pjp.getArgs();

        Throwable e = null;
        Object returnObj = null;
        long startTime = System.currentTimeMillis();
        MDC.put("requestId", HeaderContext.getHeader().getRequestId());
        try {
            log.info("接口uri开始:{}", requestURI);
            returnObj = pjp.proceed(pjp.getArgs());
            handlerRespMsg(returnObj);
        } catch (Throwable throwable) {
            e = throwable;

            // 异常处理
            if (e instanceof ApplicationException) {
                returnObj = globalExceptionHandler.handlerAppException((ApplicationException) e);
            } else {
                returnObj = globalExceptionHandler.handlerOtherException((Exception) e);
            }
        } finally {
            long execTime = System.currentTimeMillis() - startTime;

            if (e == null) {
                log.info("接口uri:{}#响应rt:{}ms#request参数:{}#response结果:{}",
                        requestURI, execTime, JSONUtil.toJsonStr(args), JSONUtil.toJsonStr(returnObj));
            } else {
                log.error("错误请求uri:{}#错误msg:{}#响应rt:{}ms#request参数:{}#response结果:{}",
                        requestURI, e.getMessage(), execTime, JSONUtil.toJsonStr(args), JSONUtil.toJsonStr(returnObj, JSONConfig.create().setTransientSupport(true)), e);
            }
            MDC.clear();
        }
        return returnObj;
    }


    private void handlerRespMsg(Object returnObj) {
        String respMsg = HeaderContext.getHeader().getRespMsg();
        if (returnObj == null || StringUtils.isBlank(respMsg)) {
            return;
        }

        if (returnObj instanceof Response<?> response) {
            response.setMessage(respMsg);
        }
    }
}
