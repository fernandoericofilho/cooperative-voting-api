package com.sicredi.votacao.services.external;

import com.sicredi.votacao.dtos.StatusVotacao;
import com.sicredi.votacao.exceptions.CpfInvalidoException;
import com.sicredi.votacao.exceptions.IntegracaoExternaIndisponivelException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

@Service
public class WebClientUserInfoClient implements UserInfoClient {

    private final WebClient webClient;
    private final Duration timeout;

    @Autowired
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
    public StatusVotacao consultar(String cpf) {
        try {
            UserInfoResponse response = webClient.get()
                .uri("/users/{cpf}", cpf)
                .retrieve()
                .bodyToMono(UserInfoResponse.class)
                .timeout(timeout)
                .retryWhen(Retry.backoff(2, Duration.ofMillis(200))
                    .filter(this::isRetryable))
                .block();
            return StatusVotacao.valueOf(response.status());
        } catch (WebClientResponseException.NotFound ex) {
            throw new CpfInvalidoException(cpf);
        } catch (Exception ex) {
            throw new IntegracaoExternaIndisponivelException(cpf, ex);
        }
    }

    private boolean isRetryable(Throwable throwable) {
        return !(throwable instanceof WebClientResponseException.NotFound);
    }

    private record UserInfoResponse(String status) {
    }
}
