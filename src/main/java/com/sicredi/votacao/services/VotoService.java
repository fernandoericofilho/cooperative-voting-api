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
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
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
        String maskedCpf = maskCpf(cpfAssociado);
        Pauta pauta = pautaService.buscarPorId(pautaId);

        if (!pauta.sessaoFoiAberta()) {
            log.warn("Tentativa de votar com sessão não aberta: pautaId={}, associado={}", pautaId, maskedCpf);
            throw new SessaoNaoAbertaException(pautaId);
        }
        if (pauta.sessaoEstaEncerrada()) {
            log.warn("Tentativa de votar com sessão encerrada: pautaId={}, associado={}", pautaId, maskedCpf);
            throw new SessaoEncerradaException(pautaId);
        }
        StatusVotacao status = userInfoClient.consultar(cpfAssociado);
        if (status == StatusVotacao.NAO_HABILITADO) {
            log.warn("Associado não habilitado para votar: pautaId={}, associado={}, status={}", pautaId, maskedCpf, status);
            throw new AssociadoNaoHabilitadoException(cpfAssociado);
        }
        log.info("Elegibilidade verificada: pautaId={}, associado={}, status={}", pautaId, maskedCpf, status);

        try {
            Voto votoRegistrado = votoRepository.save(new Voto(pautaId, cpfAssociado, voto));
            log.info("Voto registrado com sucesso: pautaId={}, associado={}, voto={}, votoId={}", pautaId, maskedCpf, voto, votoRegistrado.getId());
            return votoRegistrado;
        } catch (DataIntegrityViolationException e) {
            log.warn("Voto duplicado detectado (violação de constraint único): pautaId={}, associado={}", pautaId, maskedCpf);
            throw new VotoDuplicadoException(pautaId, cpfAssociado);
        }
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 4) {
            return "****";
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(cpf.length() - 2);
    }
}
