package com.sicredi.votacao.dtos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DomainDTOSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void pautaDTOSerializesCorrectly() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        PautaDTO pauta = new PautaDTO(1L, "Título", "Descrição", now, null, null, "NAO_INICIADA");

        String json = mapper.writeValueAsString(pauta);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"titulo\":\"Título\""));
        assertFalse(json.contains("sessaoAbertaEm")); // null fields excluded
    }

    @Test
    void votoDTOSerializesCorrectly() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        VotoDTO voto = new VotoDTO(1L, 1L, "12345678901", "SIM", now);

        String json = mapper.writeValueAsString(voto);
        assertTrue(json.contains("\"voto\":\"SIM\""));
        assertTrue(json.contains("\"cpfAssociado\":\"12345678901\""));
    }

    @Test
    void resultadoDTOSerializesCorrectly() throws Exception {
        ResultadoDTO resultado = new ResultadoDTO(1L, 10, 5, "SIM", "ENCERRADA");

        String json = mapper.writeValueAsString(resultado);
        assertTrue(json.contains("\"totalSim\":10"));
        assertTrue(json.contains("\"resultado\":\"SIM\""));
    }
}
