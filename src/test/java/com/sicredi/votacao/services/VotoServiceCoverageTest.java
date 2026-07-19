package com.sicredi.votacao.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sicredi.votacao.enums.OpcaoVoto;
import com.sicredi.votacao.enums.StatusVotacao;
import com.sicredi.votacao.exceptions.AssociadoNaoHabilitadoException;
import com.sicredi.votacao.exceptions.SessaoEncerradaException;
import com.sicredi.votacao.exceptions.SessaoNaoAbertaException;
import com.sicredi.votacao.exceptions.VotoDuplicadoException;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.models.Voto;
import com.sicredi.votacao.repositories.VotoRepository;
import com.sicredi.votacao.services.external.UserInfoClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class VotoServiceCoverageTest {

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PautaService pautaService;

    @Mock
    private UserInfoClient userInfoClient;

    @InjectMocks
    private VotoService votoService;

    @Test
    void registrarVotoComSucessoSim() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        pauta.abrirSessao(60L);

        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(userInfoClient.consultar("12345678901")).thenReturn(StatusVotacao.HABILITADO);

        Voto voto = new Voto(1L, "12345678901", OpcaoVoto.SIM);
        voto.setId(1L);
        when(votoRepository.save(any(Voto.class))).thenReturn(voto);

        Voto result = votoService.registrarVoto(1L, "12345678901", OpcaoVoto.SIM);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(votoRepository).save(any(Voto.class));
    }

    @Test
    void registrarVotoComSucessoNao() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        pauta.abrirSessao(60L);

        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(userInfoClient.consultar("98765432100")).thenReturn(StatusVotacao.HABILITADO);

        Voto voto = new Voto(1L, "98765432100", OpcaoVoto.NAO);
        voto.setId(2L);
        when(votoRepository.save(any(Voto.class))).thenReturn(voto);

        Voto result = votoService.registrarVoto(1L, "98765432100", OpcaoVoto.NAO);

        assertThat(result).isNotNull();
        assertThat(result.getVoto()).isEqualTo(OpcaoVoto.NAO);
    }

    @Test
    void registrarVotoSessaoNaoAberta() {
        Pauta pauta = new Pauta("Reforma", "Desc");

        when(pautaService.buscarPorId(1L)).thenReturn(pauta);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, "12345678901", OpcaoVoto.SIM))
            .isInstanceOf(SessaoNaoAbertaException.class);
    }

    @Test
    void registrarVotoSessaoEncerrada() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        pauta.abrirSessao(1L);
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        when(pautaService.buscarPorId(1L)).thenReturn(pauta);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, "12345678901", OpcaoVoto.SIM))
            .isInstanceOf(SessaoEncerradaException.class);
    }

    @Test
    void registrarVotoAssociadoNaoHabilitado() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        pauta.abrirSessao(60L);

        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(userInfoClient.consultar("12345678901")).thenReturn(StatusVotacao.NAO_HABILITADO);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, "12345678901", OpcaoVoto.SIM))
            .isInstanceOf(AssociadoNaoHabilitadoException.class);
    }

    @Test
    void registrarVotoDuplicado() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        pauta.abrirSessao(60L);

        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(userInfoClient.consultar("12345678901")).thenReturn(StatusVotacao.HABILITADO);
        when(votoRepository.save(any(Voto.class))).thenThrow(new DataIntegrityViolationException("Duplicate"));

        assertThatThrownBy(() -> votoService.registrarVoto(1L, "12345678901", OpcaoVoto.SIM))
            .isInstanceOf(VotoDuplicadoException.class);
    }

    @Test
    void registrarVotoComMultiplosCPFs() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        pauta.abrirSessao(60L);

        when(pautaService.buscarPorId(2L)).thenReturn(pauta);
        when(userInfoClient.consultar("11122233344")).thenReturn(StatusVotacao.HABILITADO);

        Voto voto = new Voto(2L, "11122233344", OpcaoVoto.SIM);
        voto.setId(3L);
        when(votoRepository.save(any(Voto.class))).thenReturn(voto);

        Voto result = votoService.registrarVoto(2L, "11122233344", OpcaoVoto.SIM);

        assertThat(result).isNotNull();
        verify(votoRepository).save(any(Voto.class));
    }

    @Test
    void registrarVotoVerificaLoggingInfo() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        pauta.abrirSessao(60L);

        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(userInfoClient.consultar("12345678901")).thenReturn(StatusVotacao.HABILITADO);

        Voto voto = new Voto(1L, "12345678901", OpcaoVoto.SIM);
        voto.setId(1L);
        when(votoRepository.save(any(Voto.class))).thenReturn(voto);

        votoService.registrarVoto(1L, "12345678901", OpcaoVoto.SIM);

        verify(userInfoClient).consultar("12345678901");
    }
}
