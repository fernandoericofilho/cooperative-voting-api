package com.sicredi.votacao.services;

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
import org.springframework.stereotype.Service;

@Service
public class VotoService {

    private final VotoRepository votoRepository;
    private final PautaService pautaService;
    private final UserInfoClient userInfoClient;

    public VotoService(VotoRepository votoRepository, PautaService pautaService, UserInfoClient userInfoClient) {
        this.votoRepository = votoRepository;
        this.pautaService = pautaService;
        this.userInfoClient = userInfoClient;
    }

    public Voto registrarVoto(Long pautaId, String cpfAssociado, OpcaoVoto voto) {
        Pauta pauta = pautaService.buscarPorId(pautaId);

        if (!pauta.sessaoFoiAberta()) {
            throw new SessaoNaoAbertaException(pautaId);
        }
        if (pauta.sessaoEstaEncerrada()) {
            throw new SessaoEncerradaException(pautaId);
        }
        if (votoRepository.existsByPautaIdAndCpfAssociado(pautaId, cpfAssociado)) {
            throw new VotoDuplicadoException(pautaId, cpfAssociado);
        }
        if (userInfoClient.consultar(cpfAssociado) == StatusVotacao.NAO_HABILITADO) {
            throw new AssociadoNaoHabilitadoException(cpfAssociado);
        }

        return votoRepository.save(new Voto(pautaId, cpfAssociado, voto));
    }
}
