package com.kratos.mok.pricing.app.infrastructure.security.hmac;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Component
public class PartnerHmacFilter extends OncePerRequestFilter {

    private static final String HEADER_PARTNER_ID = "X-Partner-Id";
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_NONCE = "X-Nonce";
    private static final String HEADER_SIGNATURE = "X-Signature";

    private final PartnerSecurityProperties properties;
    private final PartnerHmacService hmacService;
    private final NonceStore nonceStore;
    private final ObjectMapper objectMapper;
    //private final IpAllowlistService ipAllowlistService;
    private final RateLimitService rateLimitService;

    private static final Logger log = LoggerFactory.getLogger(PartnerHmacFilter.class);

    public PartnerHmacFilter(
            PartnerSecurityProperties properties,
            PartnerHmacService hmacService,
            NonceStore nonceStore,
            ObjectMapper objectMapper,
            //IpAllowlistService ipAllowlistService,
            RateLimitService rateLimitService
    ) {
        this.properties = properties;
        this.hmacService = hmacService;
        this.nonceStore = nonceStore;
        this.objectMapper = objectMapper;
        //this.ipAllowlistService = ipAllowlistService;
        this.rateLimitService = rateLimitService;
    }

    /*@Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return false;
    }*/

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path =
                request.getRequestURI()
                        .substring(request.getContextPath().length());

        return !path.startsWith("/v1/pricing/");
    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        log.info(
                "[HMAC] Filter exécuté : {} {}",
                request.getMethod(),
                request.getRequestURI()
        );

        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);

        /*String clientIp = ipAllowlistService.extractClientIp(wrappedRequest);
        if (!ipAllowlistService.isAllowed(clientIp, properties.allowedIps())) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "IP not allowed");
            return;
        }*/

        /*String rateLimitKey = clientIp + ":" + wrappedRequest.getServletPath();
        boolean allowed = rateLimitService.allow(
                rateLimitKey,
                properties.rateLimit().maxRequests(),
                properties.rateLimit().windowSeconds()
        );

        if (!allowed) {
            writeError(response, 429, "Too many requests");
            return;
        }*/

        String partnerId = wrappedRequest.getHeader(HEADER_PARTNER_ID);
        String timestampHeader = wrappedRequest.getHeader(HEADER_TIMESTAMP);
        String nonce = wrappedRequest.getHeader(HEADER_NONCE);
        String signature = wrappedRequest.getHeader(HEADER_SIGNATURE);

        if (isBlank(partnerId) || isBlank(timestampHeader) || isBlank(nonce) || isBlank(signature)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing authentication headers");
            return;
        }

        if (!properties.id().equals(partnerId)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unknown partner");
            return;
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampHeader);
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid timestamp");
            return;
        }

        long now = Instant.now().getEpochSecond();
        long requestTs = timestamp / 1000;

        if (Math.abs(now - requestTs) > properties.maxSkewSeconds()) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Expired request");
            return;
        }

        boolean nonceAccepted = nonceStore.registerIfAbsent(
                partnerId,
                nonce,
                now + properties.maxSkewSeconds()
        );

        if (!nonceAccepted) {
            writeError(response, HttpServletResponse.SC_CONFLICT, "Nonce already used");
            return;
        }

        String body = HttpMethod.GET.matches(wrappedRequest.getMethod())
                ? ""
                : new String(wrappedRequest.getCachedBody(), StandardCharsets.UTF_8);

        String canonicalPath = buildCanonicalPath(wrappedRequest);
        String bodyHash = hmacService.sha256Hex(body);

        String stringToSign = buildStringToSign(
                wrappedRequest.getMethod(),
                canonicalPath,
                timestampHeader,
                nonce,
                bodyHash
        );

        String expectedSignature = hmacService.sign(properties.secret(), stringToSign);

        if (!hmacService.constantTimeEquals(expectedSignature, signature)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid signature");
            return;
        }

        // 1. Définir les rôles pour correspondre au @PreAuthorize du contrôleur
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_SYSTEM"));

        // 2. Créer le jeton d'authentification propre à Spring Security
        var authentication = new UsernamePasswordAuthenticationToken(
                partnerId,
                null,
                authorities
        );

        // 3. L'injecter dans le contexte pour que les intercepteurs de méthode (@PreAuthorize) le voient
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(wrappedRequest, response);
    }

    private String buildCanonicalPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();

        if (queryString == null || queryString.isBlank()) {
            return requestUri;
        }

        return requestUri + "?" + queryString;
    }

    private String buildStringToSign(
            String method,
            String canonicalPath,
            String timestamp,
            String nonce,
            String bodyHash
    ) {
        return method + "\n" +
                canonicalPath + "\n" +
                timestamp + "\n" +
                nonce + "\n" +
                bodyHash;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        //response.getWriter().write(objectMapper.writeValueAsString(ApiResult.nok(message)));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}