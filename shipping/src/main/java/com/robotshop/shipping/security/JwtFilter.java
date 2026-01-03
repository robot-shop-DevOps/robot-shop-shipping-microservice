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
import java.util.ArrayList;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/health")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn(
                "missing authorization token"
            );
            response.sendError(
                HttpStatus.UNAUTHORIZED.value(),
                "Missing token"
            );
            return;
        }

        String token = authHeader.substring(7);

        boolean valid;
        try {
            valid = jwtUtil.validateToken(token);
        } catch (Exception e) {
            logger.error(
                "jwt validation error",
                e
            );
            response.sendError(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid token"
            );
            return;
        }

        if (!valid) {
            logger.warn(
                "invalid or expired token"
            );
            response.sendError(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid token"
            );
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "user",
                        null,
                        new ArrayList<>()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        chain.doFilter(request, response);
    }
}