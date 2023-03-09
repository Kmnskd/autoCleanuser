package com.ldap.clean.jirauserclean.api.service;

import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import com.uhome.uplus.jirauserclean.api.dto.DtoPerson;
import com.uhome.uplus.jirauserclean.api.dto.DtoTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.BinaryLogicalFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class LdapService {
    
    /**
     * LDAP客户端
     */
    @Autowired
    private LdapTemplate ldapTemplate;
    
    /**
     * 查询用户的Base
     */
    @Value("${ldap.personBase}")
    private String personBase;

    /**
     * 查询用户组的Base
     */
    @Value("${ldap.groupBase}")
    private String groupBase;
    
    /**
     * 获得团队组织架构
     * @param teamUrl 团队路径
     * @return 团队架构
     */
    public DtoTeam getTeamStruct(String teamUrl) {
        
        if (StringUtils.isEmpty(teamUrl)) {
            throw new RuntimeException("find ldap team name is null");
        }
        
        String teamName = teamUrl.split(",")[0];
        String teamBase = teamUrl.equals(teamName) ? "" : teamUrl.substring(teamName.length() + 1);
        
        List<DtoTeam> teams = getTeams(teamBase, teamName, getAllPersons());
        if (teams.size() == 0) {
            return null;
        }
        return teams.get(0);
    }
    
    /**
     * 获得团队组织架构
     * @param teamBase 团队根路径
     * @param teamName 团队名
     * @return 团队架构
     */
    public DtoTeam getTeamStruct(String teamBase, String teamName) {
        
        if (StringUtils.isEmpty(teamName)) {
            throw new RuntimeException("find ldap team name is null");
        }
        
        List<DtoTeam> teams = getTeams(teamBase, teamName, getAllPersons());
        if (teams.size() == 0) {
            return null;
        }
        return teams.get(0);
    }

    /**
     * 获得团队组织架构
     * @param teamBase 团队根路径
     * @param teamName 团队名,null时查找全部
     * @param bufferPersons 全部人员信息缓存,null时重新获取
     * @return 团队架构
     */
    private List<DtoTeam> getTeams(String teamBase, String teamName, List<DtoPerson> bufferPersons) {

        // 先查询所有的人员信息备用
        List<DtoPerson> persons = bufferPersons == null ? getAllPersons() : bufferPersons;
        
        // 构建根团队查询语句
        BinaryLogicalFilter filter = new AndFilter()
                .append(new EqualsFilter("objectclass", "posixGroup"));
        if (teamName != null) {
            filter.append(new EqualsFilter("cn", teamName));
        }
        String filterStr = filter.encode();
        
        // 查询限制在当前层级
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);

        // 查询团队列表
        List<DtoTeam> teams = ldapTemplate.search(
                teamBase, filterStr, constraints, new TeamAttributesMapper(teamBase, persons));
        
        return teams;
    }
    
    /**
     * 获得全部人员信息
     * @return 人员信息列表
     */
    public List<DtoPerson> getAllPersons() {

        // 人员查询语句
        String filter = new AndFilter()
                .append(new EqualsFilter("objectclass", "inetOrgPerson"))
                .encode();
        
        // 查询人员信息
        List<DtoPerson> persons = ldapTemplate.search(personBase, filter, new PersonAttributesMapper());
        return persons;
    }
    
    /**
     * 获得人员信息,通常用于人员登录使用
     * @param uid 人员id
     * @param pwd 人员密码
     * @return 人员信息
     */
    public DtoPerson getUser(String uid, String pwd) {

        try {
            // 判断用户名密码
            String filter = new AndFilter()
                    .append(new EqualsFilter("objectclass", "inetOrgPerson"))
                    .append(new EqualsFilter("uid", uid))
                    .encode();        
            

            // 查询当前用户信息
            List<DtoPerson> persons = ldapTemplate.search(personBase, filter, new PersonAttributesMapper());
            if (persons.size() != 1) {
                log.info("login failt:" + uid);
                return null;
            }
            DtoPerson person = persons.get(0);

      
            // TODO: 查询权限信息
            // 暂不实现
            return person;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 获得人员信息,通常用于人员登录使用
     * 
     * @param uid 人员id
     * @param pwd 人员密码
     * @return 人员信息
     */
    public DtoPerson getAuthenticationInfo(String uid, String pwd) {

        try {
            // 判断用户名密码
            String filter = new AndFilter().append(new EqualsFilter("objectclass", "inetOrgPerson"))
                    .append(new EqualsFilter("uid", uid)).encode();

            if (!ldapTemplate.authenticate(personBase, filter, pwd)) {
                log.info("login failt:" + uid);
                return null;
            }

            // 查询当前用户信息
            return getUser(uid,pwd);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    // 人员属性映射类
    private static class PersonAttributesMapper implements AttributesMapper<DtoPerson> {
        @Override
        public DtoPerson mapFromAttributes(Attributes attributes) throws NamingException {
            DtoPerson person = new DtoPerson();
            Attribute a = attributes.get("cn");
            if (a != null) {
                person.setName((String) a.get());
            }
            a = attributes.get("uid");
            if (a != null) {
                person.setUid((String) a.get());
            }
            a = attributes.get("uidNumber");
            if (a != null) {
                person.setUidNumber((String) a.get());
            }
            a = attributes.get("displayname");
            if (a != null) {
                person.setDisplayName((String) a.get());
            }
            a = attributes.get("mail");
            if (a != null) {
                person.setEmail((String) a.get());
            }
            return person;
        }
    }
    
    // 团队属性映射类
    private class TeamAttributesMapper implements AttributesMapper<DtoTeam> {
        
        private String teamBase;
        
        private List<DtoPerson> persons = null;
        
        public TeamAttributesMapper(String teamBase, List<DtoPerson> persons) {
            this.teamBase = teamBase;
            this.persons = persons;
        }
        
        @Override
        public DtoTeam mapFromAttributes(Attributes attributes) throws NamingException {
            DtoTeam team = new DtoTeam();
            Attribute a = attributes.get("cn");
            if (a != null) {
                team.setName((String)a.get());
            }
            a = attributes.get("description");
            if (a != null) {
                team.setDesc((String)a.get());
            }
            a = attributes.get("leader");
            if (a != null) {
                DtoPerson leader = findPerson(persons, (String)a.get());
                team.setLeader(leader);
            }
            a = attributes.get("memberUid");
            if (a != null) {
                NamingEnumeration<?> uids = a.getAll();
                while (uids.hasMoreElements()) {
                    DtoPerson person = findPerson(persons, uids.next().toString());
                    if (person != null) {
                        team.getPersions().add(person);
                    }
                }
            }

            List<DtoTeam> subTeams = getTeams("cn=" + team.getName() + "," + teamBase, null, persons);
            if (subTeams != null) {
                team.setSubTeams(subTeams);
            }
            return team;
        }
    }
    
    private static DtoPerson findPerson(List<DtoPerson> persons, String uid) {
        if (StringUtils.isEmpty(uid)) {
            return null;
        }
        for (DtoPerson person : persons) {
            if (uid.equals(person.getUid())) {
                return person;
            }
        }
        return null;
    }
}
