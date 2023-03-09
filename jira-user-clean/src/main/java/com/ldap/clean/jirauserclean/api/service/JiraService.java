package com.ldap.clean.jirauserclean.api.service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.uhome.uplus.jirauserclean.api.dto.DtoAuthzUser;
import com.uhome.uplus.jirauserclean.api.dto.DtoJiraUser;
import com.uhome.uplus.jirauserclean.api.dto.DtoPerson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uhome.uplus.jirauserclean.core.exception.SimpleException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class JiraService {

    @Autowired
    LdapService ldapService;
    private String jiraUrl = "http://localhost:2990";
    private String authEncode = Base64.getEncoder().encodeToString(("username" + ":" + "password").getBytes());

    private int threshhold = 495;
    private int baseline = 480;

    private String createHttpPost(String url, String entityString, boolean withAuth) throws Exception {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(jiraUrl + url);
        if (withAuth == true) {
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + authEncode);
        }
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");

        StringEntity entity = new StringEntity(entityString, Charset.forName("UTF-8"));
        entity.setContentEncoding("UTF-8");
        // 发送Json格式的数据请求
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        System.out.println("executing request " + httpPost.getRequestLine());

        HttpResponse response = httpClient.execute(httpPost);

        int statusCode = response.getStatusLine().getStatusCode();
        log.info("code:" + statusCode);
        if (statusCode != 200 && statusCode != 201) {
            throw new SimpleException("请求失败：" + url);
        }
        HttpEntity entityR = response.getEntity();
        String resultString = EntityUtils.toString(entityR, StandardCharsets.UTF_8);

        return resultString;

    }

    public void cleanUsers() throws Exception {

        String result = createHttpPost("/jira/rest/jirauserexport/1.0/search",
                "{\"searchString\":\"\",\"activeUsers\":true,\"inActiveUsers\":true,\"application\":\"jira-software\",\"pageSize\":-1,\"offSet\":-1}",
                true);

        JSONObject jsonObject = JSON.parseObject(result);

        JSONArray jsonUsers = (JSONArray) jsonObject.get("users");
        List<DtoJiraUser> users = JSON.parseArray(jsonUsers.toJSONString(), DtoJiraUser.class);
        if (users.size() >= threshhold) {
            // 到达上线，清理到baseline
            // jira-administrators, jira-software-users
            List<DtoJiraUser> jiraSoftUsers = users.stream()
                    .filter(user -> !(user.getGroups().contains("jira-administrators")||user.getGroups().contains("admin")))
                    .sorted(Comparator.comparing(DtoJiraUser::getLastLoginTime)).collect(Collectors.toList());
            int deleteCount = users.size() - baseline;
            for (int i = 0; i < deleteCount; i++) {
                log.info("delete:" + jiraSoftUsers.get(i).getName());
                deleteUserFromGroup(jiraSoftUsers.get(i));
            }
        }
    }

    private void deleteUserFromGroup(DtoJiraUser user) throws Exception {
        HttpClient httpClient = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete(
                jiraUrl + "/jira/rest/api/2/group/user?groupname=jira-software-users&username=" + user.getName());
        httpDelete.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + authEncode);

        log.info("executing request " + httpDelete.getRequestLine());
        httpClient.execute(httpDelete);
    }

    public synchronized void authzUser(DtoAuthzUser user) throws Exception {
        log.info(user.getName() + "attend to activate");
        DtoPerson person = ldapService.getAuthenticationInfo(user.getName(), user.getPwd());
        if (person == null) {
            throw new SimpleException("用户名或密码错误");
        }
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(jiraUrl + "/jira/rest/api/2/user?expand=groups&username=" + user.getName());
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + authEncode);

        HttpResponse userRes;
        log.info("executing request " + httpGet.getRequestLine());
        userRes = httpClient.execute(httpGet);

        HttpEntity userEntity = userRes.getEntity();
        String userString = EntityUtils.toString(userEntity, StandardCharsets.UTF_8);
        JSONObject jsonObject = JSON.parseObject(userString);

        JSONArray jsonGroups = (JSONArray) ((JSONObject)jsonObject.get("groups")).get("items");
        Iterator<Object> it   = jsonGroups.iterator();
        while (it.hasNext()) {
            JSONObject jsonObj = (JSONObject) it.next();
            String name=jsonObj.getString("name");
            if("jira-software-users".equals(name)) {
                throw new SimpleException("账户处于激活状态，无需再次激活");
            }        
        }        
        cleanUsers();
        createHttpPost("/jira/rest/api/2/group/user?groupname=jira-software-users",
                "{\"name\":\"" + user.getName() + "\"}", true);

        createHttpPost("/jira/rest/auth/1/session",
                "{\"username\":\"" + user.getName() + "\",\"password\":\"" + user.getPwd() + "\"}", false);

    }
}
