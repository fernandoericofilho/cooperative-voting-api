package com.sicredi.votacao.pauta;

import org.springframework.stereotype.Service;

@Service
public class PautaService {

    private static final long DURACAO_PADRAO_SEGUNDOS = 60L;

    private final PautaRepository pautaRepository;

    public PautaService(PautaRepository pautaRepository) {
        this.pautaRepository = pautaRepository;
    }

    public Pauta criarPauta(String titulo, String descricao) {
        return pautaRepository.save(new Pauta(titulo, descricao));
    }

    public Pauta buscarPorId(Long id) {
        return pautaRepository.findById(id)
            .orElseThrow(() -> new PautaNaoEncontradaException(id));
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
}
