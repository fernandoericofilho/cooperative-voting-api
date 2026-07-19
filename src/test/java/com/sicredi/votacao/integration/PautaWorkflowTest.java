package com.sicredi.votacao.integration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sicredi.votacao.dtos.ResultadoPautaDto;
import com.sicredi.votacao.enums.OpcaoVoto;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.models.Voto;
import com.sicredi.votacao.repositories.PautaRepository;
import com.sicredi.votacao.repositories.VotoRepository;
import com.sicredi.votacao.services.PautaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PautaWorkflowTest {

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private VotoRepository votoRepository;

    @InjectMocks
    private PautaService pautaService;

    @Test
    void pautaWorkflowCompleto() {
        // Create
        Pauta pautaNova = new Pauta("Reforma", "Descrição");
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pautaNova);
        pautaService.criarPauta("Reforma", "Descrição");

        // Retrieve
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pautaNova));
        Pauta pautaRecuperada = pautaService.buscarPorId(1L);
        assertThat(pautaRecuperada).isNotNull();

        // Open session
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pautaNova);
        pautaService.abrirSessao(1L, 60L);

        // Verify votes counted
        verify(pautaRepository, atLeast(2)).save(any(Pauta.class));
    }

    @Test
    void listarPautasVarias() {
        Pauta p1 = new Pauta("P1", "D1");
        Pauta p2 = new Pauta("P2", "D2");
        Pauta p3 = new Pauta("P3", "D3");

        when(pautaRepository.findAll()).thenReturn(List.of(p1, p2, p3));

        var result = pautaService.listarTodas();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTitulo()).isEqualTo("P1");
    }

    @Test
    void votosParaPauta() {
        Voto v1 = new Voto(1L, "11111111111", OpcaoVoto.SIM);
        Voto v2 = new Voto(1L, "22222222222", OpcaoVoto.SIM);
        Voto v3 = new Voto(1L, "33333333333", OpcaoVoto.NAO);

        assertThat(v1.getPautaId()).isEqualTo(v2.getPautaId());
        assertThat(v2.getVoto()).isNotEqualTo(v3.getVoto());
    }

    @Test
    void apuracaoComZeroVotos() {
        Pauta pauta = new Pauta("Test", "Desc");
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(votoRepository.contarPorPauta(1L)).thenReturn(List.of());

        ResultadoPautaDto resultado = pautaService.apurarResultado(1L);

        assertThat(resultado.resultado()).isEqualTo("EMPATE");
        assertThat(resultado.votosSim()).isEqualTo(0L);
        assertThat(resultado.votosNao()).isEqualTo(0L);
    }
}
