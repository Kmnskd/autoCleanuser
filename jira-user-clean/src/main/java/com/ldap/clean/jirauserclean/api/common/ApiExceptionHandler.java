package com.ldap.clean.jirauserclean.api.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uhome.uplus.jirauserclean.core.exception.common.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 集中处理接口访问异常
 */
@ControllerAdvice
public class ApiExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // 处理系统异常，比如404,500
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ApiResult<Object> defaultErrorHandler(HttpServletRequest req, HttpServletResponse rep, Exception e) {
        
        ApiResult<Object> result = new ApiResult<Object>();
        result.setSuccess(false);
        
        if (e instanceof org.springframework.web.HttpRequestMethodNotSupportedException) {
            result.setCode(404);
            result.setMessage("无此服务");
            rep.setStatus(404);
        } else if (e instanceof org.springframework.web.servlet.NoHandlerFoundException) {
            result.setCode(404);
            result.setMessage("无此服务");
            rep.setStatus(404);
        } else if (e instanceof BaseException) {
            // 自定义错误
            result.setCode(500);
            result.setMessage("发生错误: " + e.getMessage());
            logger.error("", e);
            rep.setStatus(500);
        } else {
            // 其他内部错误
            result.setCode(500);
            result.setMessage("服务器内部错误");
            logger.error("", e);
            rep.setStatus(500);
        }
        return result;
    }
}

