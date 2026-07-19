package com.sicredi.votacao.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sicredi.votacao.dtos.ResultadoPautaDto;
import com.sicredi.votacao.enums.OpcaoVoto;
import com.sicredi.votacao.exceptions.PautaNaoEncontradaException;
import com.sicredi.votacao.exceptions.SessaoJaAbertaException;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.models.Voto;
import com.sicredi.votacao.repositories.PautaRepository;
import com.sicredi.votacao.repositories.VotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PautaServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private VotoRepository votoRepository;

    @InjectMocks
    private PautaService pautaService;

    @Test
    void criarPautaPersiste() {
        Pauta expected = new Pauta("Reforma", "Discussão sobre artigo 5");
        when(pautaRepository.save(any(Pauta.class))).thenReturn(expected);

        Pauta result = pautaService.criarPauta("Reforma", "Discussão sobre artigo 5");

        assertThat(result).isEqualTo(expected);
        verify(pautaRepository).save(any(Pauta.class));
    }

    @Test
    void buscarPorIdRetornaPauta() {
        Pauta expected = new Pauta("Reforma", "Desc");
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(expected));

        Pauta result = pautaService.buscarPorId(1L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void buscarPorIdNaoEncontradoLancaExcecao() {
        when(pautaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pautaService.buscarPorId(999L))
                .isInstanceOf(PautaNaoEncontradaException.class);
    }

    @Test
    void abrirSessaoComDuracaoPadrao() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pauta);

        pautaService.abrirSessao(1L, 60L);

        verify(pautaRepository).save(any(Pauta.class));
        assertThat(pauta.sessaoEstaAberta()).isTrue();
    }

    @Test
    void abrirSessaoDuasVezesLancaExcecao() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        pauta.abrirSessao(60L);
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        assertThatThrownBy(() -> pautaService.abrirSessao(1L, 60L))
                .isInstanceOf(SessaoJaAbertaException.class);
    }

    @Test
    void listarPautasRetornaPage() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        Page<Pauta> expected = new PageImpl<>(List.of(pauta), PageRequest.of(0, 10), 1);
        when(pautaRepository.findAll(any(PageRequest.class))).thenReturn(expected);

        Page<Pauta> result = pautaService.listarPautas(PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).contains(pauta);
    }

    @Test
    void apurarResultadoComVotosSim() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        com.sicredi.votacao.dtos.ContagemVoto contagem1 = mock(com.sicredi.votacao.dtos.ContagemVoto.class);
        com.sicredi.votacao.dtos.ContagemVoto contagem2 = mock(com.sicredi.votacao.dtos.ContagemVoto.class);
        when(contagem1.getOpcao()).thenReturn(OpcaoVoto.SIM);
        when(contagem1.getTotal()).thenReturn(2L);
        when(contagem2.getOpcao()).thenReturn(OpcaoVoto.NAO);
        when(contagem2.getTotal()).thenReturn(1L);
        when(votoRepository.contarPorPauta(1L)).thenReturn(List.of(contagem1, contagem2));

        ResultadoPautaDto result = pautaService.apurarResultado(1L);

        assertThat(result).isNotNull();
    }
}
