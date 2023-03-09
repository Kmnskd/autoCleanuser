package com.ldap.clean.jirauserclean.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class LdapBridgeClient {
    
    private static final String codeKey = "19e0432d-f34b-4b43-b0df-4b91dcabecfe>>@123*#_+{}";

    @Getter
    @Setter
    private String getAuthenticationInfoUrl;
    
    @Getter
    @Setter
    private String getOrganizationSturctUrl;
    
    /**
     * 验证并获得人员信息
     * @param uid 人员id
     * @param pwd 人员密码
     * @return 验证人员信息
     * @throws LdapClientException Ldap客户端错误
     * @throws IOException WebAPI访问错误
     */
    public LdapPerson getAuthenticationInfo(String uid, String pwd) throws LdapClientException, IOException {
        String url = getAuthenticationInfoUrl.replace("{uid}", URLEncoder.encode(uid, "utf-8"));
        url = url.replace("{pwd}", URLEncoder.encode(pwd, "utf-8"));
        JSONObject data = getResultData(request(url));

        if (data == null) {
            return null;
        }
        
        return JSON.toJavaObject(data, LdapPerson.class);
    }
    
    /**
     * 获得组织架构信息
     * @param teamUrl 团队路径
     * @return 组织架构信息
     * @throws LdapClientException Ldap客户端错误
     * @throws IOException WebAPI访问错误
     */
    public LdapTeam getOrganizationSturct(String teamUrl) throws LdapClientException, IOException {
        String url = getOrganizationSturctUrl.replace("{teamUrl}", URLEncoder.encode(teamUrl, "utf-8"));
        JSONObject data = getResultData(request(url));
        
        if (data == null) {
            return null;
        }
        
        return JSON.toJavaObject(data, LdapTeam.class);
    }
    
    private static JSONObject getResultData(String json) throws LdapClientException {
        JSONObject result = (JSONObject) JSON.parse(json);
        
        int resultCode = result.getIntValue("code");
        String resultMsg = result.getString("message");
        if (resultCode != 200) {
            log.error("getAuthenticationInfo error, code:" + resultCode + ", message:" + resultMsg);
            throw new LdapClientException("ldap bridge api request error");
        }
        
        if (!result.getBooleanValue("success")) {
            return null;
        }
        
        return result.getJSONObject("data");
    }
    
    private static String request(String urlStr) throws IOException {
        HttpURLConnection conn = null;
        InputStream stream = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection)url.openConnection();
            
            // 设置Header
            long timestame = System.currentTimeMillis();
            conn.setRequestProperty("api-code", getCode(urlStr, timestame));
            conn.setRequestProperty("api-timestame", timestame + "");
            
            // 获得数据流
            conn.setConnectTimeout(3 * 1000);
            stream = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream, "utf-8"));
            
            // 读取数据
            StringBuilder sb = new StringBuilder();
            String buf;
            while ((buf = reader.readLine()) != null) {
                sb.append(buf);
            }
            
            // 返回字符串
            return sb.toString();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
    }
    
    // 获得签名,只有内部系统使用,简单md5(queryString + timestame + codeKey)处理
    private static String getCode(String url, long timestame) {
        String queryString = url.substring(url.indexOf("?") + 1);
        String pcode = queryString + timestame + codeKey;
        return md5(pcode);
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
