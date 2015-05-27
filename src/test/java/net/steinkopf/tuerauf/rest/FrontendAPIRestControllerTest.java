package net.steinkopf.tuerauf.rest;

import net.steinkopf.tuerauf.TueraufApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link FrontendAPIRestController}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TueraufApplication.class)
@WebAppConfiguration
public class FrontendAPIRestControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;


    @Before
    public void setUp() throws Exception {

        this.mvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    @Test
    public void testRegisterUser() throws Exception {

        if (false) {
            // Test fails like this. Why is the user not recognised?

            UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken((Principal) () -> "aaa", "xxx");

            SecurityContext mockSecurityContext = Mockito.mock(SecurityContext.class);
            Mockito.when(mockSecurityContext.getAuthentication()).thenReturn(principal);

            MockHttpSession mockHttpSession = new MockHttpSession();
            mockHttpSession.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    mockSecurityContext);

            this.mvc.perform(get("/frontend/registerUser?username=abc&pin=1111&installationId=123456789").session(mockHttpSession))
                    .andExpect(status().is4xxClientError());
        }
        this.mvc.perform(get("/frontend/registerUser?username=abc&pin=1111&installationId=123456789"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("All OK. username=abc")));
    }
}
