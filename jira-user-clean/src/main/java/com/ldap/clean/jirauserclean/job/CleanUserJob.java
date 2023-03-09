package com.ldap.clean.jirauserclean.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.uhome.uplus.jirauserclean.api.service.JiraService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@EnableScheduling
public class CleanUserJob {
    
    @Autowired
    JiraService jiraService;

    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanUser() throws Exception {
        log.info("触发定时删除任务");
        jiraService.cleanUsers();        
    }
}
