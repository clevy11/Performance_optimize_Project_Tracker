package com.example.clb.projecttracker.security.oauth2;

import com.example.clb.projecttracker.config.AppProperties;
import com.example.clb.projecttracker.security.jwt.JwtUtils;
import com.example.clb.projecttracker.util.CookieUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.example.clb.projecttracker.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final JwtUtils tokenProvider;
    private final AppProperties appProperties;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    OAuth2AuthenticationSuccessHandler(JwtUtils tokenProvider, AppProperties appProperties,
                                       HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository) {
        this.tokenProvider = tokenProvider;
        this.appProperties = appProperties;
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("✅✅✅ Entering onAuthenticationSuccess ✅✅✅");
        if (authentication != null && authentication.getPrincipal() != null) {
            logger.info("Principal type: {}", authentication.getPrincipal().getClass().getName());
            logger.info("Principal details: {}", authentication.getPrincipal());
        }

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to process success response.");
            return;
        }

        try {
            // Create JWT token
            String token = tokenProvider.generateJwtToken(authentication);
            logger.info("Generated JWT token on successful OAuth2 login.");

            // Create JSON response
            Map<String, String> tokenResponse = new HashMap<>();
            tokenResponse.put("accessToken", token);
            tokenResponse.put("tokenType", "Bearer");

            // Write JSON response to the client
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), tokenResponse);

            // Clean up authentication attributes and cookies
            clearAuthenticationAttributes(request, response);
            logger.info("✅ Successfully sent JWT token as JSON response.");

        } catch (Exception ex) {
            logger.error("Error processing OAuth2 authentication success", ex);
            throw new ServletException("Error processing OAuth2 authentication success", ex);
        }
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    // The determineTargetUrl method is no longer needed for a JSON response but kept for reference
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new IllegalArgumentException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        // For demonstration, you might want to append the token to the URL
        // This part is now handled by the JSON response
        return targetUrl;
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);
        return appProperties.getOauth2().getAuthorizedRedirectUris()
                .stream()
                .anyMatch(authorizedRedirectUri -> {
                    URI authorizedURI = URI.create(authorizedRedirectUri);
                    return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorizedURI.getPort() == clientRedirectUri.getPort();
                });
    }
}
