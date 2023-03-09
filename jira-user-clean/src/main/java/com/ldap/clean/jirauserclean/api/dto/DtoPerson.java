package com.ldap.clean.jirauserclean.api.dto;

import java.util.List;

import lombok.Data;

@Data
public class DtoPerson {
    
    // 人员编号
    private String uid;
    
    // 工号
    private String uidNumber;

    // 人员名称
    private String name;
    
    // 人员显示名称
    private String displayName;
    
    // email
    private String email;
    
    // 用户角色列表
    private List<String> roles;

    // 用户权限列表
    private List<String> permissions;
}
