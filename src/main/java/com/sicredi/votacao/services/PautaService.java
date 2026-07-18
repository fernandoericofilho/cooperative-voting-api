package com.sicredi.votacao.services;

import com.sicredi.votacao.dtos.ContagemVoto;
import com.sicredi.votacao.dtos.ResultadoPauta;
import com.sicredi.votacao.enums.OpcaoVoto;
import com.sicredi.votacao.exceptions.PautaNaoEncontradaException;
import com.sicredi.votacao.exceptions.SessaoJaAbertaException;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.repositories.PautaRepository;
import com.sicredi.votacao.repositories.VotoRepository;
import org.springframework.stereotype.Service;

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
        return pautaRepository.save(new Pauta(titulo, descricao));
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
            throw new SessaoJaAbertaException(pautaId);
        }
        long duracao = duracaoSegundos != null ? duracaoSegundos : DURACAO_PADRAO_SEGUNDOS;
        pauta.abrirSessao(duracao);
        return pautaRepository.save(pauta);
    }

    public ResultadoPauta apurarResultado(Long pautaId) {
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
        return ResultadoPauta.calcular(votosSim, votosNao);
    }
}
