package com.ldap.clean.jirauserclean.api.filter;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uhome.uplus.jirauserclean.api.common.ApiResult;

import com.alibaba.fastjson.JSON;

/**
 * 检查签名
 */
public class ApiFilter implements Filter {

    private static final String codeKey = "19e0432d-f34b-4b43-b0df-4b91dcabecfe>>@123*#_+{}";

    public ApiFilter() {

    }

    public void destroy() {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String code = httpRequest.getHeader("api-code");
        String timestamp = httpRequest.getHeader("api-timestame");

        chain.doFilter(request, response);

        chain.doFilter(request, response);
    }

    public void init(FilterConfig fConfig) throws ServletException {

    }

    private static void throwCodeError(String message, ServletResponse response) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        ApiResult<Object> result = ApiResult.createError(403, message);
        httpResponse.getWriter().write(JSON.toJSONString(result));
        httpResponse.flushBuffer();
    }

    private static String md5(String str) {
        MessageDigest md = null;
        byte[] btInput = str.getBytes();
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(btInput);
            byte[] bytes = md.digest(btInput);
            String md5 = binaryToHexString(bytes);
            return md5;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

    }

    private static String binaryToHexString(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        String hexStr = "0123456789abcdef";
        for (int i = 0; i < bytes.length; i++) {
            // 字节高4位
            hex.append(String.valueOf(hexStr.charAt((bytes[i] & 0xF0) >> 4)));
            // 字节低4位
            hex.append(String.valueOf(hexStr.charAt(bytes[i] & 0x0F)));
        }
        return hex.toString();
    }

}
