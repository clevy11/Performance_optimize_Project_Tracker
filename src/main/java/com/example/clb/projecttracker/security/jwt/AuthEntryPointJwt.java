package com.example.clb.projecttracker.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;



@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                        AuthenticationException authException) throws IOException, ServletException {
        
        // Log the error with more context
        logger.error("Authentication error for request {} {}: {}", 
                    request.getMethod(), 
                    request.getRequestURI(),
                    authException.getMessage(), 
                    authException);

        // Determine the specific authentication error
        String errorMessage = "Unauthorized";
        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
        String errorDetail = authException.getMessage();
        
        // Check for specific JWT exceptions in the cause chain
        Throwable cause = authException.getCause();
        if (cause != null) {
            if (cause instanceof ExpiredJwtException) {
                errorMessage = "Token has expired";
                errorDetail = "The provided JWT token has expired. Please log in again.";
            } else if (cause instanceof UnsupportedJwtException) {
                errorMessage = "Unsupported token";
                errorDetail = "The provided JWT token is unsupported.";
            } else if (cause instanceof MalformedJwtException) {
                errorMessage = "Invalid token";
                errorDetail = "The provided JWT token is malformed.";
            } else if (cause instanceof SignatureException) {
                errorMessage = "Invalid token signature";
                errorDetail = "The provided JWT token has an invalid signature.";
            } else if (cause instanceof IllegalArgumentException) {
                errorMessage = "Invalid token";
                errorDetail = "The provided JWT token is invalid or empty.";
            }
        }
        
        // Build the error response
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", httpStatus.value());
        errorResponse.put("error", httpStatus.getReasonPhrase());
        errorResponse.put("message", errorMessage);
        errorResponse.put("details", errorDetail);
        errorResponse.put("path", request.getRequestURI());
        
        // Add debug details in non-production environment
        if (logger.isDebugEnabled()) {
            errorResponse.put("debugMessage", authException.getMessage());
            errorResponse.put("exception", authException.getClass().getName());
        }
        
        // Set response headers and write the error response
        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                      .writeValue(response.getOutputStream(), errorResponse);
        } catch (IOException e) {
            logger.error("Failed to write error response: {}", e.getMessage(), e);
            throw new ServletException("Failed to write error response", e);
        }
    }
}
