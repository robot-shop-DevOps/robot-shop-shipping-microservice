package com.robotshop.shipping.security;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private final String secret;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secret = secret;
        logger.info("JwtUtil initialized with secret length: {}", secret != null ? secret.length() : 0);
    }

    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(this.secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(this.secret).parseClaimsJws(token);
            logger.info("Token validation SUCCESS");
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("Token EXPIRED: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.error("Token MALFORMED: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            logger.error("Token SIGNATURE INVALID: {}", e.getMessage());
            return false;
        } catch (Exception ex) {
            logger.error("Token validation FAILED: {}", ex.getMessage());
            return false;
        }
    }
}