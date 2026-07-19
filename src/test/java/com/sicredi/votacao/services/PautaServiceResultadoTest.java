package com.sicredi.votacao.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sicredi.votacao.dtos.ContagemVoto;
import com.sicredi.votacao.dtos.ResultadoPautaDto;
import com.sicredi.votacao.enums.OpcaoVoto;
import com.sicredi.votacao.exceptions.PautaNaoEncontradaException;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.repositories.PautaRepository;
import com.sicredi.votacao.repositories.VotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PautaServiceResultadoTest {

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private VotoRepository votoRepository;

    @InjectMocks
    private PautaService pautaService;

    private static class ContagemVotoMock implements ContagemVoto {
        private final OpcaoVoto opcao;
        private final Long total;

        ContagemVotoMock(OpcaoVoto opcao, Long total) {
            this.opcao = opcao;
            this.total = total;
        }

        @Override
        public OpcaoVoto getOpcao() {
            return opcao;
        }

        @Override
        public Long getTotal() {
            return total;
        }
    }

    @Test
    void apurarResultadoAprovada() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(votoRepository.contarPorPauta(1L)).thenReturn(List.of(
            new ContagemVotoMock(OpcaoVoto.SIM, 5L),
            new ContagemVotoMock(OpcaoVoto.NAO, 3L)
        ));

        ResultadoPautaDto resultado = pautaService.apurarResultado(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.votosSim()).isEqualTo(5L);
        assertThat(resultado.votosNao()).isEqualTo(3L);
        assertThat(resultado.resultado()).isEqualTo("APROVADA");
    }

    @Test
    void apurarResultadoReprovada() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(votoRepository.contarPorPauta(1L)).thenReturn(List.of(
            new ContagemVotoMock(OpcaoVoto.SIM, 2L),
            new ContagemVotoMock(OpcaoVoto.NAO, 8L)
        ));

        ResultadoPautaDto resultado = pautaService.apurarResultado(1L);

        assertThat(resultado.resultado()).isEqualTo("REPROVADA");
    }

    @Test
    void apurarResultadoEmpate() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(votoRepository.contarPorPauta(1L)).thenReturn(List.of(
            new ContagemVotoMock(OpcaoVoto.SIM, 5L),
            new ContagemVotoMock(OpcaoVoto.NAO, 5L)
        ));

        ResultadoPautaDto resultado = pautaService.apurarResultado(1L);

        assertThat(resultado.resultado()).isEqualTo("EMPATE");
    }

    @Test
    void apurarResultadoPautaNaoEncontradaLancaExcecao() {
        when(pautaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pautaService.apurarResultado(1L))
                .isInstanceOf(PautaNaoEncontradaException.class);
    }

    @Test
    void listarTodasPautas() {
        Pauta pauta1 = new Pauta("Reforma", "Desc1");
        Pauta pauta2 = new Pauta("Lei", "Desc2");
        when(pautaRepository.findAll()).thenReturn(List.of(pauta1, pauta2));

        var result = pautaService.listarTodas();

        assertThat(result).hasSize(2);
        verify(pautaRepository).findAll();
    }
}
