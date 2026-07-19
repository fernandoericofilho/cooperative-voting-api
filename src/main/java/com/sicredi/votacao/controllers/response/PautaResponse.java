package com.sicredi.votacao.controllers.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PautaResponse {
    private Long id;
    private String titulo;
    private String descricao;
    private String criadoEm;
    private String sessaoAbertaEm;
    private String sessaoFechaEm;
    private String status;

    public PautaResponse(Long id, String titulo, String descricao, String criadoEm,
                    String sessaoAbertaEm, String sessaoFechaEm, String status) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.criadoEm = criadoEm;
        this.sessaoAbertaEm = sessaoAbertaEm;
        this.sessaoFechaEm = sessaoFechaEm;
        this.status = status;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getCriadoEm() { return criadoEm; }
    public String getSessaoAbertaEm() { return sessaoAbertaEm; }
    public String getSessaoFechaEm() { return sessaoFechaEm; }
    public String getStatus() { return status; }
}
