package com.sicredi.votacao.controllers;

import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.repositories.PautaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErrorScenarioTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PautaRepository pautaRepository;

    @BeforeEach
    void setUp() {
        pautaRepository.deleteAll();
    }

    @Test
    void obterPautaInexistenteRetorna404() {
        var response = restTemplate.getForEntity("/api/v1/pautas/999", Object.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void abrirSessaoEmPautaInexistenteRetorna404() {
        String request = "{\"duracaoSegundos\": 60}";
        var response = restTemplate.postForEntity("/api/v1/pautas/999/sessoes", request, Object.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void registrarVotoEmPautaInexistenteRetorna404() {
        String request = "{\"cpfAssociado\": \"12345678901\", \"voto\": \"SIM\"}";
        var response = restTemplate.postForEntity("/api/v1/pautas/999/votos", request, Object.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void abrirSessaoDuasVezesRetorna409() {
        Pauta pauta = pautaRepository.save(new Pauta("Teste", "Desc"));
        String request = "{\"duracaoSegundos\": 60}";

        // Abre primeira vez
        var response1 = restTemplate.postForEntity("/api/v1/pautas/" + pauta.getId() + "/sessoes", request, Object.class);
        assertEquals(HttpStatus.OK, response1.getStatusCode());

        // Tenta abrir segunda vez
        var response2 = restTemplate.postForEntity("/api/v1/pautas/" + pauta.getId() + "/sessoes", request, Object.class);
        assertEquals(HttpStatus.CONFLICT, response2.getStatusCode());
    }
}
