package com.sicredi.votacao.dtos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.votacao.controllers.response.PautaResponse;
import com.sicredi.votacao.controllers.response.ResultadoVotacaoResponse;
import com.sicredi.votacao.controllers.response.VotoResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class DomainDTOSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    void pautaDTOSerializesCorrectly() throws Exception {
        String now = LocalDateTime.now().format(FORMATTER);
        PautaResponse pauta = new PautaResponse(1L, "Título", "Descrição", now, null, null, "NAO_INICIADA");

        String json = mapper.writeValueAsString(pauta);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"titulo\":\"Título\""));
        assertFalse(json.contains("sessaoAbertaEm")); // null fields excluded
    }

    @Test
    void votoDTOSerializesCorrectly() throws Exception {
        String now = LocalDateTime.now().format(FORMATTER);
        VotoResponse voto = new VotoResponse(1L, 1L, "12345678901", "SIM", now);

        String json = mapper.writeValueAsString(voto);
        assertTrue(json.contains("\"voto\":\"SIM\""));
        assertTrue(json.contains("\"cpfAssociado\":\"12345678901\""));
    }

    @Test
    void resultadoDTOSerializesCorrectly() throws Exception {
        ResultadoVotacaoResponse resultado = new ResultadoVotacaoResponse(1L, 10, 5, "SIM", "ENCERRADA");

        String json = mapper.writeValueAsString(resultado);
        assertTrue(json.contains("\"totalSim\":10"));
        assertTrue(json.contains("\"resultado\":\"SIM\""));
    }
}
