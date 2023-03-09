package com.ldap.clean.jirauserclean.lbridge.test;

import java.io.IOException;

import com.uhome.uplus.jirauserclean.client.LdapBridgeClient;
import com.uhome.uplus.jirauserclean.client.LdapClientException;
import com.uhome.uplus.jirauserclean.client.LdapPerson;
import com.uhome.uplus.jirauserclean.client.LdapTeam;

public class LdapBridgeClientTest {
    
    private static LdapBridgeClient ldapBridgeClient = new LdapBridgeClient();
    
    public static void main(String[] args) throws Exception {
        testGetAuthenticationInfo();
        testGetOrganizationSturct();
    }
    
    private static void testGetAuthenticationInfo() throws LdapClientException, IOException {
        ldapBridgeClient.setGetAuthenticationInfoUrl(
                "http://localhost:8080/ldap-bridge/api/v1/auth/getAuthenticationInfo?uid={uid}&pwd={pwd}");
        LdapPerson person = ldapBridgeClient.getAuthenticationInfo("person", "personpwd");
        System.out.println(person);
    }
    
    private static void testGetOrganizationSturct() throws LdapClientException, IOException {
        ldapBridgeClient.setGetOrganizationSturctUrl(
                "http://localhost:8080/ldap-bridge/api/v1/organization/getOrganizationSturct?teamUrl={teamUrl}");
        LdapTeam team = ldapBridgeClient.getOrganizationSturct("platform,cn=app,cn=lisi,ou=groups");
        System.out.println(team);
    }
}
