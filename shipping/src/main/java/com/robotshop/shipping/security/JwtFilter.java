package com.robotshop.shipping.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();
        logger.info("=== JwtFilter triggered ===");
        logger.info("Request Path: {}", path);

        // Skip health endpoints
        if (path.startsWith("/health")) {
            logger.info("Health endpoint detected -> skipping JWT check");
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        logger.info("Authorization Header: {}", (authHeader != null ? "Present" : "Missing"));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.error("Missing or invalid Authorization header");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing token");
            return;
        }

        String token = authHeader.substring(7);
        logger.info("Extracted Token (first 20 chars): {}", token.substring(0, Math.min(20, token.length())));

        boolean valid = jwtUtil.validateToken(token);
        logger.info("Token validation result: {}", valid);

        if (!valid) {
            logger.error("Token validation failed");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid token");
            return;
        }

        // Authentication success
        logger.info("Token VALID - setting Authentication in SecurityContext");

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("user", null, new ArrayList<>());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        logger.info("Authentication set → proceeding with request");
        chain.doFilter(request, response);
    }
}