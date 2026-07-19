package com.sicredi.votacao.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sicredi.votacao.exceptions.PautaNaoEncontradaException;
import com.sicredi.votacao.exceptions.SessaoJaAbertaException;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.repositories.PautaRepository;
import com.sicredi.votacao.repositories.VotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PautaServiceSessaoTest {

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private VotoRepository votoRepository;

    @InjectMocks
    private PautaService pautaService;

    @Test
    void abrirSessaoComSucesso() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pauta);

        Pauta result = pautaService.abrirSessao(1L, 60L);

        assertThat(result).isNotNull();
        assertThat(result.sessaoEstaAberta()).isTrue();
        verify(pautaRepository).save(any(Pauta.class));
    }

    @Test
    void abrirSessaoComDuracaoPadrao() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pauta);

        Pauta result = pautaService.abrirSessao(1L, null);

        assertThat(result).isNotNull();
        verify(pautaRepository).save(any(Pauta.class));
    }

    @Test
    void abrirSessaoJaAbertaLancaExcecao() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        pauta.abrirSessao(60L);
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        assertThatThrownBy(() -> pautaService.abrirSessao(1L, 60L))
                .isInstanceOf(SessaoJaAbertaException.class);
    }

    @Test
    void abrirSessaoPautaNaoEncontradaLancaExcecao() {
        when(pautaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pautaService.abrirSessao(1L, 60L))
                .isInstanceOf(PautaNaoEncontradaException.class);
    }
}
