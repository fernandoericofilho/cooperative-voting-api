package com.sicredi.votacao.services;

import com.sicredi.votacao.dtos.ContagemVoto;
import com.sicredi.votacao.dtos.ResultadoPautaDto;
import com.sicredi.votacao.enums.OpcaoVoto;
import com.sicredi.votacao.exceptions.PautaNaoEncontradaException;
import com.sicredi.votacao.exceptions.SessaoJaAbertaException;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.repositories.PautaRepository;
import com.sicredi.votacao.repositories.VotoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PautaService {

    private static final long DURACAO_PADRAO_SEGUNDOS = 60L;

    private final PautaRepository pautaRepository;
    private final VotoRepository votoRepository;

    public PautaService(PautaRepository pautaRepository, VotoRepository votoRepository) {
        this.pautaRepository = pautaRepository;
        this.votoRepository = votoRepository;
    }

    public Pauta criarPauta(String titulo, String descricao) {
        Pauta pauta = pautaRepository.save(new Pauta(titulo, descricao));
        log.info("Pauta criada: id={}, titulo={}", pauta.getId(), titulo);
        return pauta;
    }

    public Pauta buscarPorId(Long id) {
        return pautaRepository.findById(id)
            .orElseThrow(() -> new PautaNaoEncontradaException(id));
    }

    public java.util.List<Pauta> listarTodas() {
        return pautaRepository.findAll();
    }

    public Pauta abrirSessao(Long pautaId, Long duracaoSegundos) {
        Pauta pauta = buscarPorId(pautaId);
        if (pauta.sessaoFoiAberta()) {
            log.warn("Tentativa de abrir sessão já aberta: pautaId={}", pautaId);
            throw new SessaoJaAbertaException(pautaId);
        }
        long duracao = duracaoSegundos != null ? duracaoSegundos : DURACAO_PADRAO_SEGUNDOS;
        pauta.abrirSessao(duracao);
        Pauta pautaSalva = pautaRepository.save(pauta);
        log.info("Sessão aberta: pautaId={}, duracao={}s", pautaId, duracao);
        return pautaSalva;
    }

    public ResultadoPautaDto apurarResultado(Long pautaId) {
        buscarPorId(pautaId);
        long votosSim = 0L;
        long votosNao = 0L;
        for (ContagemVoto contagem : votoRepository.contarPorPauta(pautaId)) {
            if (contagem.getOpcao() == OpcaoVoto.SIM) {
                votosSim = contagem.getTotal();
            } else {
                votosNao = contagem.getTotal();
            }
        }
        return ResultadoPautaDto.calcular(votosSim, votosNao);
    }
}
