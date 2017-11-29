package ru.soldatenko.demo3;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.junit.Assert.*;

public class T002ClientCertificateTest {
    @Rule
    public Demo3Rule demo3 = new Demo3Rule();

    @Before
    public void init() {
        demo3.setUseDemo2ClientCertificate(true);
    }

    @Test
    public void get_public() {
        Response response = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru:8182/demo3/rest/public_resource")
                .register(JacksonJsonProvider.class)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .get();

        assertEquals(200, response.getStatus());
        assertEquals("No HTTP session should be started", 0, response.getCookies().size());
        Demo3Info info = response.readEntity(Demo3Info.class);
        assertNull(info.getSessionId());
        assertEquals("uid=bob,ou=Users,dc=demo3,dc=soldatenko,dc=ru", info.getCallerDn());
    }

    @Test
    public void start_session() {
        Response response1 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru:8182/demo3/rest/start_session")
                .register(JacksonJsonProvider.class)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("", APPLICATION_FORM_URLENCODED));
        assertEquals(200, response1.getStatus());
        NewCookie jsessionid = response1.getCookies().get("JSESSIONID");
        assertNotNull(jsessionid);

        Response response2 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru:8182/demo3/rest/public_resource")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid.toCookie())
                .accept(APPLICATION_JSON_TYPE)
                .get();
        assertEquals(200, response2.getStatus());
        Demo3Info info = response2.readEntity(Demo3Info.class);
        assertNotNull(info.getSessionId());
        assertEquals("uid=bob,ou=Users,dc=demo3,dc=soldatenko,dc=ru", info.getCallerDn());
    }

    @Test
    public void drop_session() {
        Response response1 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru:8182/demo3/rest/start_session")
                .register(JacksonJsonProvider.class)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("", APPLICATION_FORM_URLENCODED));
        assertEquals(200, response1.getStatus());
        NewCookie jsessionid = response1.getCookies().get("JSESSIONID");
        assertNotNull(jsessionid);

        Response response2 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru:8182/demo3/rest/drop_session")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid.toCookie())
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("", APPLICATION_FORM_URLENCODED));
        assertEquals(200, response2.getStatus());

        Response response3 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru:8182/demo3/rest/public_resource")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid.toCookie())
                .accept(APPLICATION_JSON_TYPE)
                .get();
        assertEquals(200, response3.getStatus());
        Demo3Info info = response3.readEntity(Demo3Info.class);
        assertNull(info.getSessionId());
        assertEquals("uid=bob,ou=Users,dc=demo3,dc=soldatenko,dc=ru", info.getCallerDn());
    }

    @Test
    public void dropped_session_should_restore_certificate_owners_authentication() {
        Response response1 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru:8182/demo3/rest/login")
                .register(JacksonJsonProvider.class)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("login=alice&password=123", APPLICATION_FORM_URLENCODED));
        assertEquals(200, response1.getStatus());
        NewCookie jsessionid = response1.getCookies().get("JSESSIONID");
        assertNotNull(jsessionid);
        Demo3Info info1 = response1.readEntity(Demo3Info.class);
        assertEquals("uid=alice,ou=Users,dc=demo3,dc=soldatenko,dc=ru", info1.getCallerDn());

        Response response2 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru:8182/demo3/rest/public_resource")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid.toCookie())
                .accept(APPLICATION_JSON_TYPE)
                .get();
        assertEquals(200, response2.getStatus());
        Demo3Info info2 = response2.readEntity(Demo3Info.class);
        assertNotNull(info2.getSessionId());
        assertEquals("uid=alice,ou=Users,dc=demo3,dc=soldatenko,dc=ru", info2.getCallerDn());

        Response response3 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru:8182/demo3/rest/drop_session")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid.toCookie())
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("", APPLICATION_FORM_URLENCODED));
        assertEquals(200, response3.getStatus());

        Response response4 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru:8182/demo3/rest/public_resource")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid.toCookie())
                .accept(APPLICATION_JSON_TYPE)
                .get();
        assertEquals(200, response4.getStatus());
        Demo3Info info3 = response4.readEntity(Demo3Info.class);
        assertNull(info3.getSessionId());
        assertEquals("uid=bob,ou=Users,dc=demo3,dc=soldatenko,dc=ru", info3.getCallerDn());
    }

    @Test
    public void client_certificate_should_give_access_to_bobs_resource() {
        Response response1 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru:8182/demo3/rest/private/bob")
                .register(JacksonJsonProvider.class)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .get();
        assertEquals(200, response1.getStatus());

        Response response2 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru:8182/demo3/rest/private/alice")
                .register(JacksonJsonProvider.class)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .get();
        assertEquals(401, response2.getStatus());
    }

    @Test
    public void impersonated_session_should_give_access_to_alices_resource() {
        Response response1 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru:8182/demo3/rest/login")
                .register(JacksonJsonProvider.class)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("login=alice&password=123", APPLICATION_FORM_URLENCODED));
        assertEquals(200, response1.getStatus());
        NewCookie jsessionid = response1.getCookies().get("JSESSIONID");

        Response response2 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru:8182/demo3/rest/private/alice")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid.toCookie())
                .accept(APPLICATION_JSON_TYPE)
                .get();
        assertEquals(200, response2.getStatus());

        Response response3 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru:8182/demo3/rest/private/bob")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid.toCookie())
                .accept(APPLICATION_JSON_TYPE)
                .get();
        assertEquals(401, response3.getStatus());
    }
}
