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
        this.secret = secret != null ? secret.trim() : null;
        logger.info("======================================");
        logger.info("JwtUtil Constructor Called");
        logger.info("Raw secret param: [{}]", secret);
        logger.info("Trimmed secret: [{}]", this.secret);
        logger.info("Secret length: {}", this.secret != null ? this.secret.length() : 0);
        
        if (this.secret != null && this.secret.length() >= 10) {
            logger.info("First 10 chars: [{}]", this.secret.substring(0, 10));
            logger.info("Last 10 chars: [{}]", this.secret.substring(this.secret.length() - 10));
        }
        
        // Log each character code to detect hidden characters
        if (this.secret != null) {
            logger.info("Secret char codes (first 20): {}", 
                this.secret.substring(0, Math.min(20, this.secret.length()))
                    .chars()
                    .mapToObj(Integer::toString)
                    .collect(java.util.stream.Collectors.joining(",")));
        }
        logger.info("======================================");
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