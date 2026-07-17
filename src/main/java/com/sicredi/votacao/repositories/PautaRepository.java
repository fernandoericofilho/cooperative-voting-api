package com.sicredi.votacao.repositories;

import com.sicredi.votacao.models.Pauta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PautaRepository extends JpaRepository<Pauta, Long> {
}
