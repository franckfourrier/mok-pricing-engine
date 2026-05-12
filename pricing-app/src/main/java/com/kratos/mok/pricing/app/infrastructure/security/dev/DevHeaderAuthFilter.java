package com.kratos.mok.pricing.app.infrastructure.security.dev;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DevHeaderAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevHeaderAuthFilter.class);

    public static final String ACTOR_HEADER = "X-Actor-Id";
    public static final String ROLES_HEADER = "X-Roles";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Si déjà authentifié (ex: via JWT), on ne touche pas.
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            String actorId = request.getHeader(ACTOR_HEADER);

            if (actorId != null && !actorId.isBlank()) {
                String rolesRaw = request.getHeader(ROLES_HEADER);

                List<SimpleGrantedAuthority> authorities = (rolesRaw == null || rolesRaw.isBlank())
                        ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        : Arrays.stream(rolesRaw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                var auth = new UsernamePasswordAuthenticationToken(
                        actorId.trim(),
                        "N/A",
                        authorities);

                SecurityContextHolder.getContext().setAuthentication(auth);

                LOGGER.debug(
                        "Dev authentication injected for actor={} authorities={}",
                        actorId,
                        authorities
                );
            }
            else {

                LOGGER.debug(
                        "No {} header found for request {} {}",
                        ACTOR_HEADER,
                        request.getMethod(),
                        request.getRequestURI()
                );
            }
        }

        filterChain.doFilter(request, response);
    }

}
