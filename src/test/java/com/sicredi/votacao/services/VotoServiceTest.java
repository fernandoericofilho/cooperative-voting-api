package com.sicredi.votacao.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.sicredi.votacao.dtos.StatusVotacao;
import com.sicredi.votacao.exceptions.AssociadoNaoHabilitadoException;
import com.sicredi.votacao.exceptions.SessaoEncerradaException;
import com.sicredi.votacao.exceptions.SessaoNaoAbertaException;
import com.sicredi.votacao.exceptions.VotoDuplicadoException;
import com.sicredi.votacao.models.OpcaoVoto;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.models.Voto;
import com.sicredi.votacao.repositories.VotoRepository;
import com.sicredi.votacao.services.external.UserInfoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VotoServiceTest {

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PautaService pautaService;

    @Mock
    private UserInfoClient userInfoClient;

    private VotoService votoService;

    @BeforeEach
    void setUp() {
        votoService = new VotoService(votoRepository, pautaService, userInfoClient);
    }

    @Test
    void registrarVotoSemSessaoAbertaLancaExcecao() {
        Pauta pauta = new Pauta("Pauta", "desc");
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, "11122233344", OpcaoVoto.SIM))
            .isInstanceOf(SessaoNaoAbertaException.class);
    }

    @Test
    void registrarVotoComSessaoEncerradaLancaExcecao() {
        Pauta pauta = new Pauta("Pauta", "desc");
        pauta.abrirSessao(0L);
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, "11122233344", OpcaoVoto.SIM))
            .isInstanceOf(SessaoEncerradaException.class);
    }

    @Test
    void registrarVotoDuplicadoLancaExcecao() {
        Pauta pauta = new Pauta("Pauta", "desc");
        pauta.abrirSessao(60L);
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(votoRepository.existsByPautaIdAndCpfAssociado(1L, "11122233344")).thenReturn(true);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, "11122233344", OpcaoVoto.SIM))
            .isInstanceOf(VotoDuplicadoException.class);
    }

    @Test
    void registrarVotoComAssociadoNaoHabilitadoLancaExcecao() {
        Pauta pauta = new Pauta("Pauta", "desc");
        pauta.abrirSessao(60L);
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(votoRepository.existsByPautaIdAndCpfAssociado(1L, "11122233344")).thenReturn(false);
        when(userInfoClient.consultar("11122233344")).thenReturn(StatusVotacao.UNABLE_TO_VOTE);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, "11122233344", OpcaoVoto.SIM))
            .isInstanceOf(AssociadoNaoHabilitadoException.class);
    }

    @Test
    void registrarVotoValidoPersisteEDevolveVoto() {
        Pauta pauta = new Pauta("Pauta", "desc");
        pauta.abrirSessao(60L);
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(votoRepository.existsByPautaIdAndCpfAssociado(1L, "11122233344")).thenReturn(false);
        when(userInfoClient.consultar("11122233344")).thenReturn(StatusVotacao.ABLE_TO_VOTE);
        when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Voto voto = votoService.registrarVoto(1L, "11122233344", OpcaoVoto.SIM);

        assertThat(voto.getCpfAssociado()).isEqualTo("11122233344");
        assertThat(voto.getVoto()).isEqualTo(OpcaoVoto.SIM);
    }
}
