/*
package com.kratos.gateway.security;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IpAllowlistService {

    public boolean isAllowed(String clientIp, List<String> allowedIps) {
        if (clientIp == null || allowedIps == null || allowedIps.isEmpty()) {
            return false;
        }
        return allowedIps.contains(clientIp);
    }

    public String extractClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}*/
