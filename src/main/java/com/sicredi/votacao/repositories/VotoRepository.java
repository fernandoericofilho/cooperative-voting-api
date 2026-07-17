package com.sicredi.votacao.repositories;

import com.sicredi.votacao.dtos.ContagemVoto;
import com.sicredi.votacao.models.Voto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    boolean existsByPautaIdAndCpfAssociado(Long pautaId, String cpfAssociado);

    @Query("select v.voto as opcao, count(v) as total from Voto v where v.pautaId = :pautaId group by v.voto")
    List<ContagemVoto> contarPorPauta(@Param("pautaId") Long pautaId);
}
