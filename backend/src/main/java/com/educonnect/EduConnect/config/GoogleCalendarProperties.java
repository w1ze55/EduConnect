package com.educonnect.EduConnect.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConfigurationProperties(prefix = "google.calendar")
@Data
public class GoogleCalendarProperties {
    private boolean enabled;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String frontendRedirectUrl;
    private String scope = "https://www.googleapis.com/auth/calendar";
    private String defaultCalendarId = "primary";
    private String timeZone = "America/Sao_Paulo";
    private int defaultDurationMinutes = 60;

    public boolean isConfigured() {
        return enabled
            && StringUtils.hasText(clientId)
            && StringUtils.hasText(clientSecret)
            && StringUtils.hasText(redirectUri)
            && StringUtils.hasText(frontendRedirectUrl);
    }
}
