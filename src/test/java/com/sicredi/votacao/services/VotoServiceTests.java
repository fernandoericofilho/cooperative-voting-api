package com.sicredi.votacao.services;

import com.sicredi.votacao.enums.OpcaoVoto;
import com.sicredi.votacao.enums.StatusVotacao;
import com.sicredi.votacao.exceptions.SessaoEncerradaException;
import com.sicredi.votacao.exceptions.SessaoNaoAbertaException;
import com.sicredi.votacao.exceptions.VotoDuplicadoException;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.models.Voto;
import com.sicredi.votacao.repositories.PautaRepository;
import com.sicredi.votacao.repositories.VotoRepository;
import com.sicredi.votacao.services.external.UserInfoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class VotoServiceTests {

    @Autowired
    private VotoService votoService;

    @Autowired
    private VotoRepository votoRepository;

    @Autowired
    private PautaRepository pautaRepository;

    @MockBean
    private UserInfoClient userInfoClient;

    private Pauta pauta;

    @BeforeEach
    void setUp() {
        pautaRepository.deleteAll();
        votoRepository.deleteAll();
        pauta = pautaRepository.save(new Pauta("Teste", "Descrição"));
    }

    @Test
    void registrarVotoSessaoNaoAberta() {
        assertThrows(SessaoNaoAbertaException.class, () -> {
            votoService.registrarVoto(pauta.getId(), "12345678901", OpcaoVoto.SIM);
        });
    }

    @Test
    void registrarVotoSessaoEncerrada() throws InterruptedException {
        pauta.abrirSessao(1);
        pautaRepository.save(pauta);
        Thread.sleep(1100);

        assertThrows(SessaoEncerradaException.class, () -> {
            votoService.registrarVoto(pauta.getId(), "12345678901", OpcaoVoto.SIM);
        });
    }

    @Test
    void registrarVotoDuplicado() {
        pauta.abrirSessao(60);
        pautaRepository.save(pauta);

        when(userInfoClient.consultar("12345678901"))
                .thenReturn(StatusVotacao.HABILITADO);

        votoService.registrarVoto(pauta.getId(), "12345678901", OpcaoVoto.SIM);

        assertThrows(VotoDuplicadoException.class, () -> {
            votoService.registrarVoto(pauta.getId(), "12345678901", OpcaoVoto.NAO);
        });
    }

    @Test
    void registrarVotoComSucesso() {
        pauta.abrirSessao(60);
        pautaRepository.save(pauta);

        when(userInfoClient.consultar("12345678901"))
                .thenReturn(StatusVotacao.HABILITADO);

        Voto voto = votoService.registrarVoto(pauta.getId(), "12345678901", OpcaoVoto.SIM);

        assertNotNull(voto);
        assertEquals(pauta.getId(), voto.getPautaId());
        assertEquals("12345678901", voto.getCpfAssociado());
    }
}
