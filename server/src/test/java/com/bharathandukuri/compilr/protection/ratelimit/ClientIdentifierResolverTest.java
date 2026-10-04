package com.bharathandukuri.compilr.protection.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIdentifierResolverTest {

    private final ClientIdentifierResolver resolver = new ClientIdentifierResolver();

    @Test
    @DisplayName("Prefers X-Client-Id header when present")
    void prefersClientIdHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Client-Id", "user-uuid-999");
        request.addHeader("X-Session-Id", "session-888");
        request.setRemoteAddr("192.168.1.50");

        String id = resolver.resolveClientId(request);
        assertThat(id).isEqualTo("client:user-uuid-999");
    }

    @Test
    @DisplayName("Uses X-Session-Id header when X-Client-Id is missing")
    void usesSessionIdWhenClientIdMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Session-Id", "sess-xyz-123");
        request.setRemoteAddr("192.168.1.50");

        String id = resolver.resolveClientId(request);
        assertThat(id).isEqualTo("session:sess-xyz-123");
    }

    @Test
    @DisplayName("Uses first IP from X-Forwarded-For when client headers missing")
    void usesForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178");
        request.setRemoteAddr("10.0.0.1");

        String id = resolver.resolveClientId(request);
        assertThat(id).isEqualTo("ip:203.0.113.195");
    }

    @Test
    @DisplayName("Falls back to remote address when headers missing")
    void fallsBackToRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");

        String id = resolver.resolveClientId(request);
        assertThat(id).isEqualTo("ip:192.168.1.100");
    }

    @Test
    @DisplayName("Handles null request gracefully")
    void handlesNullRequest() {
        String id = resolver.resolveClientId(null);
        assertThat(id).isEqualTo("unknown-client");
    }
}
