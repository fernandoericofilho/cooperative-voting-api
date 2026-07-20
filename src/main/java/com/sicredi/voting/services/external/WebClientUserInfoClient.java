package com.sicredi.voting.services.external;

import com.sicredi.voting.enums.VotingStatus;
import org.springframework.beans.factory.annotation.Value;
import com.sicredi.voting.exceptions.InvalidCpfException;
import com.sicredi.voting.exceptions.ExternalIntegrationUnavailableException;
import com.sicredi.voting.services.util.CpfUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

@Slf4j
public class WebClientUserInfoClient implements UserInfoClient {

    private final WebClient webClient;
    private final Duration timeout;

    public WebClientUserInfoClient(
        WebClient.Builder webClientBuilder,
        @Value("${app.external.user-info-url}") String baseUrl,
        @Value("${app.external.timeout-seconds}") long timeoutSeconds
    ) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    WebClientUserInfoClient(WebClient webClient, Duration timeout) {
        this.webClient = webClient;
        this.timeout = timeout;
    }

    @Override
    @CircuitBreaker(name = "userInfoClient", fallbackMethod = "checkFallback")
    public VotingStatus check(String cpf) {
        String maskedCpf = CpfUtils.mask(cpf);
        log.info("Checking member eligibility: {}", maskedCpf);

        try {
            UserInfoResponse response = webClient.get()
                .uri("/users/{cpf}", cpf)
                .retrieve()
                .bodyToMono(UserInfoResponse.class)
                .timeout(timeout)
                .retryWhen(Retry.backoff(2, Duration.ofMillis(200))
                    .filter(this::isRetryable))
                .block();

            VotingStatus status = parseStatus(response.status());
            log.info("Eligibility obtained successfully: member={}, status={}", maskedCpf, status);
            return status;
        } catch (WebClientResponseException.NotFound ex) {
            log.warn("CPF not found in external service: {}", maskedCpf);
            throw new InvalidCpfException(cpf);
        } catch (IllegalArgumentException ex) {
            // Unknown status from external service - log and re-throw to distinguish
            // from service unavailability (this is a data issue, not a connectivity issue)
            log.error("Unknown status from external service for member {}: {}", maskedCpf, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Error checking member eligibility {}: {}", maskedCpf, ex.getMessage());
            throw new ExternalIntegrationUnavailableException(cpf, ex);
        }
    }

    /**
     * Parses the status string from external service response to VotingStatus enum.
     * Explicitly handles the two valid statuses to avoid masking unknown values.
     *
     * @param status the status string from external service
     * @return the corresponding VotingStatus enum value
     * @throws IllegalArgumentException if status is not recognized
     */
    private VotingStatus parseStatus(String status) {
        if (status == null) {
            log.warn("Null status received from external service");
            throw new IllegalArgumentException("Null status from external service");
        }

        return switch (status.toUpperCase()) {
            case "ELIGIBLE" -> VotingStatus.ELIGIBLE;
            case "NOT_ELIGIBLE" -> VotingStatus.NOT_ELIGIBLE;
            default -> {
                log.warn("Unknown status from external service: '{}'", status);
                throw new IllegalArgumentException("Unknown status: " + status);
            }
        };
    }

    private boolean isRetryable(Throwable throwable) {
        return !(throwable instanceof WebClientResponseException.NotFound);
    }

    VotingStatus checkFallback(String cpf, Exception ex) {
        String maskedCpf = CpfUtils.mask(cpf);
        log.error("Circuit breaker open for eligibility. Failure: {}", ex.getMessage());
        throw new ExternalIntegrationUnavailableException("Eligibility service unavailable", ex);
    }

    private record UserInfoResponse(String status) {
    }
}
