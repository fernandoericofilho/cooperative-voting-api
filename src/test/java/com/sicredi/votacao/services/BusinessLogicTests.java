package com.sicredi.votacao.services;

import com.sicredi.votacao.enums.OpcaoVoto;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.models.Voto;
import com.sicredi.votacao.repositories.PautaRepository;
import com.sicredi.votacao.repositories.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {"spring.profiles.active=test"})
class BusinessLogicTests {

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private VotoRepository votoRepository;

    private Pauta pauta;

    @BeforeEach
    void setUp() {
        pautaRepository.deleteAll();
        votoRepository.deleteAll();
        pauta = pautaRepository.save(new Pauta("Teste", "Descrição"));
    }

    @Test
    void sessaoNaoAbertaInicialmente() {
        assertFalse(pauta.sessaoEstaAberta());
    }

    @Test
    void abrirSessaoComDuracaoPadrao() {
        pauta.abrirSessao(60);
        assertTrue(pauta.sessaoEstaAberta());
        assertNotNull(pauta.getSessaoAbertaEm());
        assertNotNull(pauta.getSessaoFechaEm());
    }

    @Test
    void sessaoEncerradaAposDuracao() throws InterruptedException {
        pauta.abrirSessao(1);
        assertTrue(pauta.sessaoEstaAberta());
        Thread.sleep(1100);
        assertFalse(pauta.sessaoEstaAberta());
    }

    @Test
    void votoDuplicadoMesmaOpcao() {
        pauta.abrirSessao(60);
        pautaRepository.save(pauta);

        votoRepository.save(new Voto(pauta.getId(), "12345678901", OpcaoVoto.SIM));

        assertThrows(Exception.class, () -> {
            votoRepository.save(new Voto(pauta.getId(), "12345678901", OpcaoVoto.NAO));
        });
    }

    @Test
    void votosDiferentesCPFs() {
        pauta.abrirSessao(60);
        pautaRepository.save(pauta);

        votoRepository.save(new Voto(pauta.getId(), "12345678901", OpcaoVoto.SIM));
        votoRepository.save(new Voto(pauta.getId(), "98765432109", OpcaoVoto.NAO));

        var contagem = votoRepository.contarPorPauta(pauta.getId());
        assertEquals(2, contagem.size());
    }

    @Test
    void contagemVotosPorOpcao() {
        pauta.abrirSessao(60);
        pautaRepository.save(pauta);

        votoRepository.save(new Voto(pauta.getId(), "11111111111", OpcaoVoto.SIM));
        votoRepository.save(new Voto(pauta.getId(), "22222222222", OpcaoVoto.SIM));
        votoRepository.save(new Voto(pauta.getId(), "33333333333", OpcaoVoto.NAO));

        var contagem = votoRepository.contarPorPauta(pauta.getId());
        assertEquals(2, contagem.size());
    }
}
