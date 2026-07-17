package com.sicredi.votacao.pauta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class PautaServiceTest {

    @Autowired
    private PautaService pautaService;

    @Test
    void criarPautaPersisteEDevolveComId() {
        Pauta pauta = pautaService.criarPauta("Reforma do estatuto", "Discussão sobre o artigo 5");

        assertThat(pauta.getId()).isNotNull();
        assertThat(pautaService.buscarPorId(pauta.getId()).getTitulo()).isEqualTo("Reforma do estatuto");
    }

    @Test
    void buscarPorIdInexistenteLancaExcecao() {
        assertThatThrownBy(() -> pautaService.buscarPorId(999_999L))
            .isInstanceOf(PautaNaoEncontradaException.class);
    }
}
