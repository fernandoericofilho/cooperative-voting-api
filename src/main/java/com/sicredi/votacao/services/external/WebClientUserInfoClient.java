package com.sicredi.votacao.services.external;

import com.sicredi.votacao.enums.StatusVotacao;
import com.sicredi.votacao.exceptions.CpfInvalidoException;
import com.sicredi.votacao.exceptions.IntegracaoExternaIndisponivelException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

@Slf4j
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
        String maskedCpf = maskCpf(cpf);
        log.info("Consultando elegibilidade do associado: {}", maskedCpf);

        try {
            UserInfoResponse response = webClient.get()
                .uri("/users/{cpf}", cpf)
                .retrieve()
                .bodyToMono(UserInfoResponse.class)
                .timeout(timeout)
                .retryWhen(Retry.backoff(2, Duration.ofMillis(200))
                    .filter(this::isRetryable))
                .block();

            StatusVotacao status = parseStatus(response.status());
            log.info("Elegibilidade obtida com sucesso: associado={}, status={}", maskedCpf, status);
            return status;
        } catch (WebClientResponseException.NotFound ex) {
            log.warn("CPF não encontrado no serviço externo: {}", maskedCpf);
            throw new CpfInvalidoException(cpf);
        } catch (IllegalArgumentException ex) {
            // Unknown status from external service - log and re-throw to distinguish
            // from service unavailability (this is a data issue, not a connectivity issue)
            log.error("Status desconhecido do serviço externo para associado {}: {}", maskedCpf, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Erro ao consultar elegibilidade do associado {}: {}", maskedCpf, ex.getMessage());
            throw new IntegracaoExternaIndisponivelException(cpf, ex);
        }
    }

    /**
     * Parses the status string from external service response to StatusVotacao enum.
     * Explicitly handles the two valid statuses to avoid masking unknown values.
     *
     * @param status the status string from external service
     * @return the corresponding StatusVotacao enum value
     * @throws IllegalArgumentException if status is not recognized
     */
    private StatusVotacao parseStatus(String status) {
        if (status == null) {
            log.warn("Status null recebido do serviço externo");
            throw new IllegalArgumentException("Status nulo do serviço externo");
        }

        return switch (status.toUpperCase()) {
            case "HABILITADO" -> StatusVotacao.HABILITADO;
            case "NAO_HABILITADO" -> StatusVotacao.NAO_HABILITADO;
            default -> {
                log.warn("Status desconhecido do serviço externo: '{}'", status);
                throw new IllegalArgumentException("Status desconhecido: " + status);
            }
        };
    }

    private boolean isRetryable(Throwable throwable) {
        return !(throwable instanceof WebClientResponseException.NotFound);
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 4) {
            return "****";
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(cpf.length() - 2);
    }

    private record UserInfoResponse(String status) {
    }
}
