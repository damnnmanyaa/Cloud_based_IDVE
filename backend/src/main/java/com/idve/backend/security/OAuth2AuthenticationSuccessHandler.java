package com.idve.backend.security;

import com.idve.backend.service.AuthService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.oauth2.redirect-uri}")
    private String frontendRedirectUri;

    public OAuth2AuthenticationSuccessHandler(@Lazy AuthService authService,
                                              OAuth2AuthorizedClientService authorizedClientService) {
        this.authService = authService;
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = extractEmail(authentication, oauth2User);

        if (email == null || email.isBlank()) {
            String errorUrl = UriComponentsBuilder
                .fromUriString(frontendRedirectUri)
                .queryParam("error", "oauth_email_not_found")
                .build()
                .toUriString();
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
            return;
        }

        String name = extractName(oauth2User, email);
        String token = authService.loginWithOAuth(email, name);

        String targetUrl = UriComponentsBuilder
            .fromUriString(frontendRedirectUri)
            .queryParam("token", token)
            .build()
            .toUriString();

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String extractEmail(Authentication authentication, OAuth2User oauth2User) {
        Object email = oauth2User.getAttributes().get("email");
        if (email instanceof String emailStr && !emailStr.isBlank()) {
            return emailStr;
        }

        if (authentication instanceof OAuth2AuthenticationToken oauthToken
            && "github".equalsIgnoreCase(oauthToken.getAuthorizedClientRegistrationId())) {
            return fetchGithubEmail(oauthToken);
        }

        return null;
    }

    private String fetchGithubEmail(OAuth2AuthenticationToken oauthToken) {
        try {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
            );
            if (client == null || client.getAccessToken() == null) {
                return null;
            }

            String accessToken = client.getAccessToken().getTokenValue();
            URL url = new URL("https://api.github.com/user/emails");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Accept", "application/vnd.github+json");

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                return null;
            }

            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            )) {
                StringBuilder payload = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    payload.append(line);
                }

                List<Map<String, Object>> emails = objectMapper.readValue(
                    payload.toString(),
                    new TypeReference<List<Map<String, Object>>>() {}
                );

                String primaryVerified = selectEmail(emails, true, true);
                if (primaryVerified != null) {
                    return primaryVerified;
                }

                String verified = selectEmail(emails, null, true);
                if (verified != null) {
                    return verified;
                }

                return selectEmail(emails, null, null);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private String selectEmail(List<Map<String, Object>> emails, Boolean primary, Boolean verified) {
        for (Map<String, Object> entry : emails) {
            if (primary != null && !primary.equals(entry.get("primary"))) {
                continue;
            }
            if (verified != null && !verified.equals(entry.get("verified"))) {
                continue;
            }
            Object email = entry.get("email");
            if (email instanceof String emailStr && !emailStr.isBlank()) {
                return emailStr;
            }
        }
        return null;
    }

    private String extractName(OAuth2User oauth2User, String email) {
        Map<String, Object> attrs = oauth2User.getAttributes();
        Object name = attrs.get("name");
        if (name instanceof String nameStr && !nameStr.isBlank()) {
            return nameStr;
        }

        Object login = attrs.get("login");
        if (login instanceof String loginStr && !loginStr.isBlank()) {
            return loginStr;
        }

        return email;
    }
}
