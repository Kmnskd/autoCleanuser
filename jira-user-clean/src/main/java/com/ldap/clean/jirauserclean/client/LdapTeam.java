package com.ldap.clean.jirauserclean.client;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class LdapTeam {

    // 团队名称
    private String name;
    
    // 团队描述
    private String desc;
    
    // 团队长
    private LdapPerson leader;
    
    // 团队成员
    private List<LdapPerson> persions = new ArrayList<LdapPerson>();
    
    // 下属团队
    private List<LdapTeam> subTeams = new ArrayList<LdapTeam>();
}
