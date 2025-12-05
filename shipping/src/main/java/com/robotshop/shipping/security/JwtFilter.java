package com.robotshop.shipping.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();
        logger.info("=== JWT Filter - Path: {}", path);

        // ***** BYPASS JWT FOR HEALTH CHECKS *****
        if (path.startsWith("/health")) {
            logger.info("Health check - bypassing JWT");
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        logger.info("Authorization Header: {}", authHeader != null ? "Present" : "Missing");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing token");
            return;
        }

        String token = authHeader.substring(7);
        logger.info("Token extracted (first 20 chars): {}", token.substring(0, Math.min(20, token.length())));

        boolean isValid = jwtUtil.validateToken(token);
        logger.info("Token validation result: {}", isValid);

        if (!isValid) {
            logger.error("Token validation failed!");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid token");
            return;
        }

        logger.info("Token valid - proceeding with request");
        chain.doFilter(request, response);
    }

}