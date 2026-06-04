package com.kratos.mok.pricing.app.infrastructure.security.dev;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DevHeaderAuthFilter extends OncePerRequestFilter {

    private static final String ACTOR_HEADER = "X-Actor-Id";
    private static final String ROLES_HEADER = "X-Roles";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            String actorId = request.getHeader(ACTOR_HEADER);

            if (actorId != null && !actorId.isBlank()) {

                String rolesRaw = request.getHeader(ROLES_HEADER);

                List<SimpleGrantedAuthority> authorities =
                        (rolesRaw == null || rolesRaw.isBlank())
                                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                                : Arrays.stream(rolesRaw.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isBlank())
                                .distinct()
                                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                var auth = new UsernamePasswordAuthenticationToken(
                        actorId.trim(),
                        "N/A",
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}