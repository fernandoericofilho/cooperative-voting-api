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

    @Test
    void abrirSessaoComDuracaoInformadaDefineJanela() {
        Pauta pauta = pautaService.criarPauta("Pauta A", "desc");

        Pauta atualizada = pautaService.abrirSessao(pauta.getId(), 120L);

        assertThat(atualizada.sessaoFoiAberta()).isTrue();
        assertThat(atualizada.getSessaoFechaEm())
            .isAfter(atualizada.getSessaoAbertaEm().plusSeconds(119))
            .isBefore(atualizada.getSessaoAbertaEm().plusSeconds(121));
    }

    @Test
    void abrirSessaoSemDuracaoUsaSessentaSegundosPorDefault() {
        Pauta pauta = pautaService.criarPauta("Pauta B", "desc");

        Pauta atualizada = pautaService.abrirSessao(pauta.getId(), null);

        assertThat(atualizada.getSessaoFechaEm())
            .isAfter(atualizada.getSessaoAbertaEm().plusSeconds(59))
            .isBefore(atualizada.getSessaoAbertaEm().plusSeconds(61));
    }

    @Test
    void abrirSessaoDuasVezesLancaExcecao() {
        Pauta pauta = pautaService.criarPauta("Pauta C", "desc");
        pautaService.abrirSessao(pauta.getId(), null);

        assertThatThrownBy(() -> pautaService.abrirSessao(pauta.getId(), null))
            .isInstanceOf(SessaoJaAbertaException.class);
    }
}
