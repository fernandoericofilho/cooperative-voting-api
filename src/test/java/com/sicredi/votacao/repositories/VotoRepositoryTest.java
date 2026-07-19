package com.sicredi.votacao.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sicredi.votacao.dtos.ContagemVoto;
import com.sicredi.votacao.enums.OpcaoVoto;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.models.Voto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

@SpringBootTest
class VotoRepositoryTest {

    @Autowired
    private VotoRepository votoRepository;

    @Autowired
    private PautaRepository pautaRepository;

    @Test
    void votoDuplicadoParaMesmaPautaECpfViolaConstraintUnica() {
        Pauta pauta = pautaRepository.save(new Pauta("Pauta X", "desc"));
        votoRepository.saveAndFlush(new Voto(pauta.getId(), "11122233344", OpcaoVoto.SIM));

        assertThatThrownBy(() ->
            votoRepository.saveAndFlush(new Voto(pauta.getId(), "11122233344", OpcaoVoto.NAO)))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void contarPorPautaAgregaPorOpcao() {
        Pauta pauta = pautaRepository.save(new Pauta("Pauta Y", "desc"));
        votoRepository.saveAndFlush(new Voto(pauta.getId(), "11111111111", OpcaoVoto.SIM));
        votoRepository.saveAndFlush(new Voto(pauta.getId(), "22222222222", OpcaoVoto.SIM));
        votoRepository.saveAndFlush(new Voto(pauta.getId(), "33333333333", OpcaoVoto.NAO));

        var contagem = votoRepository.contarPorPauta(pauta.getId());

        assertThat(contagem).hasSize(2);
        assertThat(contagem.stream().filter(c -> c.getOpcao() == OpcaoVoto.SIM).findFirst().orElseThrow().getTotal())
            .isEqualTo(2L);
        assertThat(contagem.stream().filter(c -> c.getOpcao() == OpcaoVoto.NAO).findFirst().orElseThrow().getTotal())
            .isEqualTo(1L);
    }
}
