package com.bharathandukuri.compilr.protection.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ClientIdentifierResolver {

    public static final String HEADER_CLIENT_ID = "X-Client-Id";
    public static final String HEADER_SESSION_ID = "X-Session-Id";
    public static final String HEADER_FORWARDED_FOR = "X-Forwarded-For";

    public String resolveClientId(HttpServletRequest request) {
        if (request == null) {
            return "unknown-client";
        }

        String clientId = request.getHeader(HEADER_CLIENT_ID);
        if (clientId != null && !clientId.isBlank()) {
            return "client:" + clientId.trim();
        }

        String sessionId = request.getHeader(HEADER_SESSION_ID);
        if (sessionId != null && !sessionId.isBlank()) {
            return "session:" + sessionId.trim();
        }

        String forwardedFor = request.getHeader(HEADER_FORWARDED_FOR);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] ips = forwardedFor.split(",");
            if (ips.length > 0 && !ips[0].isBlank()) {
                return "ip:" + ips[0].trim();
            }
        }

        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null && !remoteAddr.isBlank()) {
            return "ip:" + remoteAddr.trim();
        }

        return "unknown-client";
    }
}
