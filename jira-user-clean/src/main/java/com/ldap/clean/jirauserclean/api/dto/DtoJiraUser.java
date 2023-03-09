package com.ldap.clean.jirauserclean.api.dto;

import java.util.List;

import lombok.Data;

@Data
public class DtoJiraUser {
    
    // 组
    private List<String> groups;

    // 上次登录时间
    private long lastLoginTime;
    
    // id
    private String name;
    
}
