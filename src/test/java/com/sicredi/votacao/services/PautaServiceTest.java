package com.sicredi.votacao.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sicredi.votacao.dtos.ResultadoPautaDto;
import com.sicredi.votacao.enums.OpcaoVoto;
import com.sicredi.votacao.exceptions.PautaNaoEncontradaException;
import com.sicredi.votacao.exceptions.SessaoJaAbertaException;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.models.Voto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PautaServiceTest {

    @Autowired
    private PautaService pautaService;

    @Autowired
    private com.sicredi.votacao.repositories.VotoRepository votoRepository;

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

    @Test
    void apurarResultadoContabilizaVotosEDefineAprovada() {
        Pauta pauta = pautaService.criarPauta("Pauta Resultado", "desc");
        votoRepository.save(new Voto(pauta.getId(), "11111111111", OpcaoVoto.SIM));
        votoRepository.save(new Voto(pauta.getId(), "22222222222", OpcaoVoto.SIM));
        votoRepository.save(new Voto(pauta.getId(), "33333333333", OpcaoVoto.NAO));

        ResultadoPautaDto resultado = pautaService.apurarResultado(pauta.getId());

        assertThat(resultado.votosSim()).isEqualTo(2L);
        assertThat(resultado.votosNao()).isEqualTo(1L);
        assertThat(resultado.resultado()).isEqualTo("APROVADA");
    }

    @Test
    void apurarResultadoSemVotosDaEmpateZeroAZero() {
        Pauta pauta = pautaService.criarPauta("Pauta Vazia", "desc");

        ResultadoPautaDto resultado = pautaService.apurarResultado(pauta.getId());

        assertThat(resultado.votosSim()).isZero();
        assertThat(resultado.votosNao()).isZero();
        assertThat(resultado.resultado()).isEqualTo("EMPATE");
    }
}
