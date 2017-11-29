package ru.soldatenko.demo3;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Base64;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.junit.Assert.*;

public class T004LoginTest {
    @Rule
    public Demo3Rule demo3 = new Demo3Rule();

    @Test
    public void form_login() {
        Response response = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/login")
                .register(JacksonJsonProvider.class)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("login=alice&password=123", APPLICATION_FORM_URLENCODED));
        assertEquals(200, response.getStatus());
        Demo3Info info = response.readEntity(Demo3Info.class);
        assertNotNull(info.getSessionId());
        assertEquals("uid=alice,ou=Users,dc=demo3,dc=soldatenko,dc=ru", info.getCallerDn());
    }

    @Test
    public void failed_form_login() {
        Response response = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/login")
                .register(JacksonJsonProvider.class)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("login=alice&password=---wrong----password----", APPLICATION_FORM_URLENCODED));
        assertEquals(401, response.getStatus());
        String body = response.readEntity(String.class);
        assertThat(body, CoreMatchers.containsString("error"));
        assertThat(body, CoreMatchers.containsString("Authorization failed"));
    }

    @Test
    public void change_login_in_one_session_form_login() {
        Response response1 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/login")
                .register(JacksonJsonProvider.class)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("login=alice&password=123", APPLICATION_FORM_URLENCODED));
        assertEquals(200, response1.getStatus());
        NewCookie jsessionid = response1.getCookies().get("JSESSIONID");
        Demo3Info info1 = response1.readEntity(Demo3Info.class);
        String sessionId = info1.getSessionId();

        Response response2 = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/login")
                .register(JacksonJsonProvider.class)
                .request()
                .cookie(jsessionid)
                .accept(APPLICATION_JSON_TYPE)
                .post(Entity.entity("login=bob&password=123", APPLICATION_FORM_URLENCODED));
        assertEquals(200, response2.getStatus());
        Demo3Info info2 = response2.readEntity(Demo3Info.class);
        assertEquals("uid=bob,ou=Users,dc=demo3,dc=soldatenko,dc=ru", info2.getCallerDn());
        assertEquals(sessionId, info2.getSessionId());
    }

    @Test
    public void failed_basic_auth() {
        Response response = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/public_resource")
                .register(JacksonJsonProvider.class)
                .request()
                .header("Authorization", "Basic "
                        + Base64.getEncoder().encodeToString("alice:-----wrong-password----".getBytes()))
                .accept(APPLICATION_JSON_TYPE)
                .get();
        assertEquals(401, response.getStatus());
        String body = response.readEntity(String.class);
        assertThat(body, CoreMatchers.containsString("Authorization failed"));
    }

    @Test
    public void bearer_token_authorization() {
        Response response = ClientBuilder.newClient()
                .target("https://demo3.soldatenko.ru/demo3/rest/public_resource")
                .register(JacksonJsonProvider.class)
                .request()
                .header("Authorization", "Bearer uuw8u2382wr2")
                .accept(APPLICATION_JSON_TYPE)
                .get();
        assertEquals(200, response.getStatus());
        Demo3Info info = response.readEntity(Demo3Info.class);
        assertEquals("uid=bob,ou=Users,dc=demo3,dc=soldatenko,dc=ru", info.getCallerDn());
    }
}
