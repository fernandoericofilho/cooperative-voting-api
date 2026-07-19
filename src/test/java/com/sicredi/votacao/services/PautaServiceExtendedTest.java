package com.sicredi.votacao.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.repositories.PautaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PautaServiceExtendedTest {

    @Mock
    private PautaRepository pautaRepository;

    @InjectMocks
    private PautaService pautaService;

    @Test
    void buscarPautaPorId() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        Pauta result = pautaService.buscarPorId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTitulo()).isEqualTo("Reforma");
    }

    @Test
    void criarPauta() {
        Pauta pauta = new Pauta("Pauta Test", "Description Test");
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pauta);

        Pauta result = pautaService.criarPauta("Pauta Test", "Description Test");

        assertThat(result).isNotNull();
        assertThat(result.getTitulo()).isEqualTo("Pauta Test");
        verify(pautaRepository).save(any(Pauta.class));
    }
}
