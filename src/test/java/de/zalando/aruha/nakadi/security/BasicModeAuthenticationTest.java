package de.zalando.aruha.nakadi.security;


import com.google.common.collect.ImmutableList;
import de.zalando.aruha.nakadi.Application;
import de.zalando.aruha.nakadi.config.SecuritySettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest
@DirtiesContext(classMode = AFTER_CLASS)
public class BasicModeAuthenticationTest extends AuthenticationTest {

    static {
        authMode = SecuritySettings.AuthMode.BASIC;
    }

    private static final List<Endpoint> endpoints = ImmutableList.of(
            new Endpoint(GET, "/event-types"),
            new Endpoint(POST, "/event-types"),
            new Endpoint(GET, "/event-types/foo"),
            new Endpoint(PUT, "/event-types/foo"),
            new Endpoint(POST, "/event-types/foo/events"),
            new Endpoint(GET, "/event-types/foo/events"),
            new Endpoint(GET, "/event-types/foo/partitions"),
            new Endpoint(GET, "/event-types/foo/partitions/bar"),
            new Endpoint(GET, "/metrics"));

    @Test
    public void basicAuthMode() throws Exception {
        endpoints.forEach(this::checkHasOnlyAccessByUidScope);
        mockMvc.perform(get("/health")).andExpect(status().isOk());
    }

    private void checkHasOnlyAccessByUidScope(final Endpoint endpoint) {
        try {
            // basic uid scope
            mockMvc.perform(addTokenHeader(endpoint.toRequestBuilder(), TOKEN_WITH_UID_SCOPE))
                    .andExpect(STATUS_NOT_401_OR_403);

            // token with random scope
            mockMvc.perform(addTokenHeader(endpoint.toRequestBuilder(), TOKEN_WITH_RANDOM_SCOPE))
                    .andExpect(status().isForbidden());

            // no token at all
            mockMvc.perform(endpoint.toRequestBuilder())
                    .andExpect(status().isUnauthorized());
        }
        catch (Exception e) {
            throw new AssertionError("Error occurred when calling endpoint: " + endpoint, e);
        }
        catch (AssertionError e) {
            throw new AssertionError("Assertion failed for endpoint: " + endpoint, e);
        }
    }

}
