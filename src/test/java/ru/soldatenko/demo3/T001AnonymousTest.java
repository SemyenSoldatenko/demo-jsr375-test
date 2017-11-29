package ru.soldatenko.demo3;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.junit.Assert.*;

public class T001AnonymousTest {
    @Rule
    public Demo3Rule demo3 = new Demo3Rule();

    @Test
    public void get_public() {
        Response response = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/public_resource")
                .register(JacksonJsonProvider.class)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .get();

        assertEquals(200, response.getStatus());
        assertEquals("No HTTP session should be started", 0, response.getCookies().size());
        Demo3Info info = response.readEntity(Demo3Info.class);
        assertNull(info.getCallerDn());
    }

    @Test
    public void start_session() {
        Response response1 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/start_session")
                .register(JacksonJsonProvider.class)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("", APPLICATION_FORM_URLENCODED));
        assertEquals(200, response1.getStatus());
        NewCookie jsessionid = response1.getCookies().get("JSESSIONID");
        assertNotNull(jsessionid);

        Response response2 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/public_resource")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid.toCookie())
                .accept(APPLICATION_JSON_TYPE)
                .get();
        assertEquals(200, response2.getStatus());
        Demo3Info info = response2.readEntity(Demo3Info.class);
        assertNotNull(info.getSessionId());
        assertNull(info.getCallerDn());
    }

    @Test
    public void drop_session() {
        Response response1 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/start_session")
                .register(JacksonJsonProvider.class)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("", APPLICATION_FORM_URLENCODED));
        assertEquals(200, response1.getStatus());
        NewCookie jsessionid = response1.getCookies().get("JSESSIONID");
        assertNotNull(jsessionid);

        Response response2 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/drop_session")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid.toCookie())
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("", APPLICATION_FORM_URLENCODED));
        assertEquals(200, response2.getStatus());

        Response response3 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/public_resource")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid.toCookie())
                .accept(APPLICATION_JSON_TYPE)
                .get();
        assertEquals(200, response3.getStatus());
        Demo3Info info = response3.readEntity(Demo3Info.class);
        assertNull(info.getSessionId());
    }

    @Test
    public void dropped_session_should_logout_user() {
        Response response1 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/login")
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
                .target("https://demo3.soldatenko.ru/demo3/rest/drop_session")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid.toCookie())
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("", APPLICATION_FORM_URLENCODED));
        assertEquals(200, response2.getStatus());

        Response response3 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/public_resource")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid.toCookie())
                .accept(APPLICATION_JSON_TYPE)
                .get();
        assertEquals(200, response3.getStatus());
        Demo3Info info3 = response3.readEntity(Demo3Info.class);
        assertNull(info3.getSessionId());
        assertNull(info3.getCallerDn());
    }

    @Test
    public void anonymous_should_not_have_access_to_private_resources() {
        Response response1 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/private/bob")
                .register(JacksonJsonProvider.class)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .get();
        assertEquals(401, response1.getStatus());

        Response response2 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/private/alice")
                .register(JacksonJsonProvider.class)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .get();
        assertEquals(401, response2.getStatus());
    }
}
