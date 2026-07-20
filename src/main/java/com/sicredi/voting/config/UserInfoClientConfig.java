package com.sicredi.voting.config;

import com.sicredi.voting.services.external.UserInfoClient;
import com.sicredi.voting.services.external.WebClientUserInfoClient;
import com.sicredi.voting.services.external.MockUserInfoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class UserInfoClientConfig {

    @Bean
    public UserInfoClient userInfoClient(
            @Value("${app.mock.enabled}") boolean mockEnabled,
            WebClient.Builder webClientBuilder,
            @Value("${app.external.user-info-url}") String userInfoUrl,
            @Value("${app.external.timeout-seconds}") long timeoutSeconds) {

        if (mockEnabled) {
            log.warn("============================================================");
            log.warn("⚠️  MOCK MODE ENABLED - User eligibility validation is MOCKED");
            log.warn("External service: {} is OFFLINE", userInfoUrl);
            log.warn("All CPFs will return ELIGIBLE status");
            log.warn("Set 'app.mock.enabled: false' in application.yml to use real service");
            log.warn("============================================================");
            return new MockUserInfoClient();
        } else {
            log.info("Using real external service: {}", userInfoUrl);
            return new WebClientUserInfoClient(webClientBuilder, userInfoUrl, timeoutSeconds);
        }
    }
}
