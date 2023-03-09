package com.ldap.clean.jirauserclean.api.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DtoTeam {

    // 团队名称
    private String name;
    
    // 团队描述
    private String desc;
    
    // 团队长
    private DtoPerson leader;
    
    // 团队成员
    private List<DtoPerson> persions = new ArrayList<DtoPerson>();
    
    // 下属团队
    private List<DtoTeam> subTeams = new ArrayList<DtoTeam>();
}
