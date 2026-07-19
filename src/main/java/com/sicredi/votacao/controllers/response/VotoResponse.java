package com.sicredi.votacao.controllers.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VotoResponse {
    private Long id;
    private Long pautaId;
    private String cpfAssociado;
    private String voto;
    private String criadoEm;

    public VotoResponse(Long id, Long pautaId, String cpfAssociado, String voto, String criadoEm) {
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
    public String getCriadoEm() { return criadoEm; }
}
