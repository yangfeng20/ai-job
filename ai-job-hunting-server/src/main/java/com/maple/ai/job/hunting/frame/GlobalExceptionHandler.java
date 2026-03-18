package com.maple.ai.job.hunting.frame;


import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.frame.exp.ApplicationException;
import com.maple.ai.job.hunting.model.common.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author 杨锋
 * @date 2022/6/25 23:37
 * desc: 全局异常处理
 */

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {


    /**
     * 处理自定义的app异常
     *
     * @param e Exception
     * @return Response
     */
    @ExceptionHandler(value = ApplicationException.class)
    @ResponseBody
    public Response<?> handlerAppException(ApplicationException e) {
        return Response.error(e.getBizCode(), e.getMessage());
    }


    /**
     * 处理参数校验异常
     *
     * @param e Exception
     * @return Response
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public Response<?> handlerArgException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        if (fieldError == null) {
            return Response.error(e.getMessage());
        }
        return Response.error(fieldError.getDefaultMessage());
    }


    /**
     * 未捕获的其他异常处理
     *
     * @param e Exception
     * @return Response
     */
    @ExceptionHandler
    @ResponseBody
    public Response<?> handlerOtherException(Exception e) {
        log.error("错误请求uri:{}-全局异常处理未捕获异常:", HeaderContext.getHeader().getRequestUri(), e);
        return Response.error("服务端错误,请稍后重试");
    }

    /**
     * 未捕获的其他异常处理
     *
     * @param e Exception
     * @return Response
     */
    public Response<?> handlerOtherException(Exception e, String uri) {
        log.error("错误请求uri:{}-全局异常处理未捕获异常:", uri, e);
        return Response.error("服务端错误,请稍后重试");
    }
}
