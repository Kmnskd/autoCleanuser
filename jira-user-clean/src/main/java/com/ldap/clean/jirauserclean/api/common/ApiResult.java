package com.ldap.clean.jirauserclean.api.common;

import java.io.Serializable;
import lombok.Data;

import org.apache.http.HttpStatus;

/**
 * API接口返回数据结构
 */
@Data
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功标志
     */
    protected boolean success = true;

    /**
     * 返回消息
     */
    protected String message = "操作成功！";

    /**
     * 返回状态码
     */
    protected Integer code = 0;

    /**
     * 返回数据
     */
    protected T data;

    /**
     * 时间戳
     */
    protected long timestamp = System.currentTimeMillis();

    public ApiResult() {

    }

    public ApiResult<T> success() {
        this.code = HttpStatus.SC_OK;
        this.success = true;
        return this;
    }

    public ApiResult<T> serverError() {
        this.message = "服务器内部错误";
        this.code = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        this.success = false;
        return this;
    }

    public static <T> ApiResult<T> createSuccess(String message) {
        ApiResult<T> r = new ApiResult<T>();
        r.success = true;
        r.code = HttpStatus.SC_OK;
        r.message = message;
        return r;
    }

    public static <T> ApiResult<T> createSuccess() {
        ApiResult<T> r = new ApiResult<T>();
        r.success = true;
        r.code = HttpStatus.SC_OK;
        return r;
    }

    public static <T> ApiResult<T> createSuccess(T data) {
        ApiResult<T> r = new ApiResult<T>();
        r.success = true;
        r.code = HttpStatus.SC_OK;
        r.data = data;
        return r;
    }

    public static <T> ApiResult<T> createError(int code, String message) {
        ApiResult<T> r = new ApiResult<T>();
        r.success = false;
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> ApiResult<T> createServerError() {
        return createError(HttpStatus.SC_INTERNAL_SERVER_ERROR, "服务器内部错误");
    }

    public static <T> ApiResult<T> createUnAuthError() {
        return createError(HttpStatus.SC_UNAUTHORIZED, "用户未登录");
    }

    public static <T> ApiResult<T> createForbiddenError() {
        return createError(HttpStatus.SC_UNAUTHORIZED, "无权访问");
    }
}