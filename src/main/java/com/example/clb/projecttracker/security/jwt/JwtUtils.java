package com.example.clb.projecttracker.security.jwt;

import com.example.clb.projecttracker.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    private static final String TOKEN_TYPE = "JWT";
    private static final String TOKEN_ISSUER = "project-tracker-api";
    private static final String TOKEN_AUDIENCE = "project-tracker-client";
    private static final String ROLES_CLAIM = "roles";
    private static final String AUTH_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Value("${app.auth.tokenSecret}")
    private String jwtSecret;

    @Value("${app.auth.tokenExpirationMsec}")
    private int jwtExpirationMs;

    @Value("${app.auth.refreshTokenExpirationMsec}")
    private int refreshTokenExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateJwtToken(Authentication authentication) {
        String username;
        List<String> roles = new ArrayList<>();
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            // Handle regular UserDetails
            username = userDetails.getUsername();
            roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        } else if (principal instanceof OAuth2User oauth2User) {
            // Handle OAuth2 user
            username = oauth2User.getName();
            // For OAuth2 users, you might want to add a default role or get it from OAuth2 attributes
            roles = oauth2User.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            if (roles.isEmpty()) {
                // Add default role for OAuth2 users if needed
                roles.add("ROLE_USER");
            }
        } else {
            throw new IllegalArgumentException("Unsupported principal type: " + principal.getClass().getName());
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(TOKEN_ISSUER)
                .setAudience(TOKEN_AUDIENCE)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim(ROLES_CLAIM, roles)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
                
        return claims.getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
                
            // Additional validation
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken)
                .getBody();
                
            // Validate token issuer
            if (!claims.getIssuer().equals(TOKEN_ISSUER)) {
                logger.warn("Invalid token issuer: {}", claims.getIssuer());
                return false;
            }
            
            // Validate token audience
            if (!claims.getAudience().equals(TOKEN_AUDIENCE)) {
                logger.warn("Invalid token audience: {}", claims.getAudience());
                return false;
            }
            
            return true;
            
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error validating JWT token: {}", e.getMessage());
        }

        return false;
    }
    
    public String getJwtFromRequest(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        
        if (StringUtils.hasText(header) && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }
        
        logger.debug("No JWT token found in request headers");
        return null;
    }
    
    public String getJwtFromHeader(String header) {
        if (StringUtils.hasText(header) && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
