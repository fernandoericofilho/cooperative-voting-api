package com.sicredi.votacao.mappers;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.votacao.controllers.response.VotoResponse;
import com.sicredi.votacao.enums.OpcaoVoto;
import com.sicredi.votacao.models.Voto;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

class VotoMapperCoverageTest {

    private final VotoMapper mapper = new VotoMapper();

    @Test
    void mapearVotoComDateTimeFormatado() {
        Voto voto = new Voto(1L, "12345678901", OpcaoVoto.SIM);
        voto.setId(1L);

        VotoResponse response = mapper.toVotoDTO(voto);

        assertThat(response.getCriadoEm()).isNotNull();
        assertThat(response.getCriadoEm()).contains("T");
    }

    @Test
    void mapearVotoTodosCampos() {
        Voto voto = new Voto(2L, "98765432100", OpcaoVoto.NAO);
        voto.setId(2L);

        VotoResponse response = mapper.toVotoDTO(voto);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getPautaId()).isEqualTo(2L);
        assertThat(response.getCpfAssociado()).isEqualTo("98765432100");
        assertThat(response.getVoto()).isEqualTo("NAO");
        assertThat(response.getCriadoEm()).isNotNull();
    }

    @Test
    void mapearVotoDateTimeIsoFormat() {
        Voto voto = new Voto(3L, "11122233344", OpcaoVoto.SIM);
        voto.setId(3L);

        VotoResponse response = mapper.toVotoDTO(voto);

        assertThat(response.getCriadoEm()).contains("T");
        assertThat(response.getCriadoEm()).doesNotContain("Z");
    }

    @Test
    void mapearVotoSimOpcao() {
        Voto voto = new Voto(4L, "44455566677", OpcaoVoto.SIM);
        voto.setId(4L);

        VotoResponse response = mapper.toVotoDTO(voto);

        assertThat(response.getVoto()).isEqualTo("SIM");
        assertThat(response.getCriadoEm()).isNotNull();
    }

    @Test
    void mapearVotoNaoOpcao() {
        Voto voto = new Voto(5L, "77788899900", OpcaoVoto.NAO);
        voto.setId(5L);

        VotoResponse response = mapper.toVotoDTO(voto);

        assertThat(response.getVoto()).isEqualTo("NAO");
        assertThat(response.getCriadoEm()).isNotNull();
    }

    @Test
    void mapearVotoFormatDateTimeConsistency() {
        Voto voto1 = new Voto(1L, "12345678901", OpcaoVoto.SIM);
        voto1.setId(1L);

        Voto voto2 = new Voto(2L, "98765432100", OpcaoVoto.NAO);
        voto2.setId(2L);

        VotoResponse response1 = mapper.toVotoDTO(voto1);
        VotoResponse response2 = mapper.toVotoDTO(voto2);

        assertThat(response1.getCriadoEm()).isNotNull();
        assertThat(response2.getCriadoEm()).isNotNull();
        assertThat(response1.getCriadoEm()).contains("T");
        assertThat(response2.getCriadoEm()).contains("T");
    }

    @Test
    void mapearVotoDateTimeNotEmpty() {
        Voto voto = new Voto(10L, "55566677788", OpcaoVoto.SIM);
        voto.setId(10L);

        VotoResponse response = mapper.toVotoDTO(voto);

        assertThat(response.getCriadoEm()).isNotEmpty();
        assertThat(response.getCriadoEm()).isNotBlank();
    }

    @Test
    void mapearVotoCompleteMapping() {
        Voto voto = new Voto(20L, "99900011122", OpcaoVoto.NAO);
        voto.setId(20L);

        VotoResponse response = mapper.toVotoDTO(voto);

        assertThat(response)
            .hasFieldOrPropertyWithValue("id", 20L)
            .hasFieldOrPropertyWithValue("pautaId", 20L)
            .hasFieldOrPropertyWithValue("cpfAssociado", "99900011122")
            .hasFieldOrPropertyWithValue("voto", "NAO");
        assertThat(response.getCriadoEm()).isNotNull();
    }
}
