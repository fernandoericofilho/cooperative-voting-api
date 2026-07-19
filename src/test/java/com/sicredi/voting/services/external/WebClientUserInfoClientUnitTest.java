package com.sicredi.voting.services.external;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;

class WebClientUserInfoClientUnitTest {

    @Test
    void clientCanBeCreated() {
        WebClient webClient = WebClient.builder().build();
        WebClientUserInfoClient client = new WebClientUserInfoClient(webClient, Duration.ofSeconds(3));

        assertThat(client).isNotNull();
    }
}
