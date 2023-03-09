package com.ldap.clean.jirauserclean.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.uhome.uplus.jirauserclean.api.dto.DtoAuthzUser;
import com.uhome.uplus.jirauserclean.api.service.JiraService;

@Controller
@RequestMapping("/jira")
@CrossOrigin(origins = "*")
public class JiraUserController {
    
    @Autowired
    JiraService jiraService;

    
    
    @PostMapping(value="/activate.do")
    @ResponseBody
    public String authzUser(DtoAuthzUser user) throws Exception {
        jiraService.authzUser(user);
        return "success";
    }
}
