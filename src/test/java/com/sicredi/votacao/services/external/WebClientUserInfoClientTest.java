package com.sicredi.votacao.services.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sicredi.votacao.enums.StatusVotacao;
import com.sicredi.votacao.exceptions.CpfInvalidoException;
import com.sicredi.votacao.exceptions.IntegracaoExternaIndisponivelException;
import java.io.IOException;
import java.time.Duration;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class WebClientUserInfoClientTest {

    private MockWebServer server;
    private WebClientUserInfoClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        WebClient webClient = WebClient.builder().baseUrl(server.url("/").toString()).build();
        client = new WebClientUserInfoClient(webClient, Duration.ofSeconds(1));
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void cpfHabilitadoRetornaHabilitado() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"status\":\"HABILITADO\"}")
            .addHeader("Content-Type", "application/json"));

        assertThat(client.consultar("19839091069")).isEqualTo(StatusVotacao.HABILITADO);
    }

    @Test
    void cpfNaoHabilitadoRetornaNaoHabilitado() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"status\":\"NAO_HABILITADO\"}")
            .addHeader("Content-Type", "application/json"));

        assertThat(client.consultar("62289608068")).isEqualTo(StatusVotacao.NAO_HABILITADO);
    }

    @Test
    void cpfInexistenteLancaCpfInvalidoException() {
        server.enqueue(new MockResponse().setResponseCode(404));

        assertThatThrownBy(() -> client.consultar("00000000000"))
            .isInstanceOf(CpfInvalidoException.class);
    }

    @Test
    void falhaPersistenteLancaIntegracaoExternaIndisponivelException() {
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        assertThatThrownBy(() -> client.consultar("11111111111"))
            .isInstanceOf(IntegracaoExternaIndisponivelException.class);
    }
}
