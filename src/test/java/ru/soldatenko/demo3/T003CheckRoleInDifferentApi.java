package ru.soldatenko.demo3;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.junit.Assert.assertEquals;

/**
 * Java EE have several places to access security services.
 * This test validate how it works for form based authorization and Client Certificate bases authorization
 */
public class T003CheckRoleInDifferentApi {
    @Rule
    public Demo3Rule demo3 = new Demo3Rule();

    private Cookie jsessionid;

    @Before
    public void init() {
        demo3.setUseDemo2ClientCertificate(false);
        Response response = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/login")
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("login=alice&password=123", APPLICATION_FORM_URLENCODED));
        assertEquals(200, response.getStatus());
        jsessionid = response.getCookies().get("JSESSIONID").toCookie();
    }

    @After
    public void tearDown() {
        Response response = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/drop_session")
                .request()
                .cookie(jsessionid)
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("", APPLICATION_FORM_URLENCODED));
        assertEquals(200, response.getStatus());
    }

    @Test
    public void javax_ws_rs_core_SecurityContext() {
        Response response = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/javax.ws.rs.core.SecurityContext")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid)
                .accept(APPLICATION_JSON_TYPE)
                .get();

        assertEquals(200, response.getStatus());
        Demo3Info info = response.readEntity(Demo3Info.class);
        assertEquals("uid=alice,ou=Users,dc=demo3,dc=soldatenko,dc=ru", info.getCallerDn());
        assertEquals(true, info.getGroupMembership().get("cn=All,ou=Groups,dc=demo3,dc=soldatenko,dc=ru"));
        assertEquals(true, info.getGroupMembership().get("cn=cph,ou=Groups,dc=demo3,dc=soldatenko,dc=ru"));
        assertEquals(false, info.getGroupMembership().get("cn=sf,ou=Groups,dc=demo3,dc=soldatenko,dc=ru"));
    }

    @Test
    public void javax_ejb_SessionContext() {
        Response response = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/javax.ejb.SessionContext")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid)
                .accept(APPLICATION_JSON_TYPE)
                .get();

        assertEquals(200, response.getStatus());
        Demo3Info info = response.readEntity(Demo3Info.class);
        assertEquals("uid=alice,ou=Users,dc=demo3,dc=soldatenko,dc=ru", info.getCallerDn());
        assertEquals(true, info.getGroupMembership().get("cn=All,ou=Groups,dc=demo3,dc=soldatenko,dc=ru"));
        assertEquals(true, info.getGroupMembership().get("cn=cph,ou=Groups,dc=demo3,dc=soldatenko,dc=ru"));
        assertEquals(false, info.getGroupMembership().get("cn=sf,ou=Groups,dc=demo3,dc=soldatenko,dc=ru"));
    }

    @Test
    public void javax_servlet_http_HttpServlet() {
        Response response = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/servlet/javax.servlet.http.HttpServlet")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid)
                .accept(APPLICATION_JSON_TYPE)
                .get();

        assertEquals(200, response.getStatus());
        Demo3Info info = response.readEntity(Demo3Info.class);
        assertEquals("uid=alice,ou=Users,dc=demo3,dc=soldatenko,dc=ru", info.getCallerDn());
        assertEquals(true, info.getGroupMembership().get("cn=All,ou=Groups,dc=demo3,dc=soldatenko,dc=ru"));
        assertEquals(true, info.getGroupMembership().get("cn=cph,ou=Groups,dc=demo3,dc=soldatenko,dc=ru"));
        assertEquals(false, info.getGroupMembership().get("cn=sf,ou=Groups,dc=demo3,dc=soldatenko,dc=ru"));
    }

    @Test
    public void javax_security_enterprise_SecurityContext() {
        Response response = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/javax.security.enterprise.SecurityContext")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid)
                .accept(APPLICATION_JSON_TYPE)
                .get();

        assertEquals(200, response.getStatus());
        Demo3Info info = response.readEntity(Demo3Info.class);
        assertEquals("uid=alice,ou=Users,dc=demo3,dc=soldatenko,dc=ru", info.getCallerDn());
        assertEquals(true, info.getGroupMembership().get("cn=All,ou=Groups,dc=demo3,dc=soldatenko,dc=ru"));
        assertEquals(true, info.getGroupMembership().get("cn=cph,ou=Groups,dc=demo3,dc=soldatenko,dc=ru"));
        assertEquals(false, info.getGroupMembership().get("cn=sf,ou=Groups,dc=demo3,dc=soldatenko,dc=ru"));
    }
}
