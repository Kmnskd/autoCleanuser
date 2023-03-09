package com.ldap.clean.jirauserclean.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uhome.uplus.jirauserclean.api.common.ApiResult;
import com.uhome.uplus.jirauserclean.api.dto.DtoAuthzUser;
import com.uhome.uplus.jirauserclean.api.dto.DtoPerson;
import com.uhome.uplus.jirauserclean.api.service.JiraService;

@RestController
@RequestMapping("/jira")
@CrossOrigin(origins = "*")
public class JiraUserAction {
    
    @Autowired
    JiraService jiraService;

    
    @PostMapping("/activeUser")
    public ApiResult<DtoPerson> authzUser(DtoAuthzUser user) throws Exception {
        jiraService.authzUser(user);
        return ApiResult.createSuccess();
    }
}
