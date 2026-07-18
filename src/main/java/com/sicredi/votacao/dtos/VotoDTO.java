package com.sicredi.votacao.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VotoDTO {
    private Long id;
    private Long pautaId;
    private String cpfAssociado;
    private String voto;
    private LocalDateTime criadoEm;

    public VotoDTO(Long id, Long pautaId, String cpfAssociado, String voto, LocalDateTime criadoEm) {
        this.id = id;
        this.pautaId = pautaId;
        this.cpfAssociado = cpfAssociado;
        this.voto = voto;
        this.criadoEm = criadoEm;
    }

    // Getters
    public Long getId() { return id; }
    public Long getPautaId() { return pautaId; }
    public String getCpfAssociado() { return cpfAssociado; }
    public String getVoto() { return voto; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
