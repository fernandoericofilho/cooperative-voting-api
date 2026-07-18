package com.sicredi.votacao.controllers.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PautaResponse {
    private Long id;
    private String titulo;
    private String descricao;
    private LocalDateTime criadoEm;
    private LocalDateTime sessaoAbertaEm;
    private LocalDateTime sessaoFechaEm;
    private String status;

    public PautaResponse(Long id, String titulo, String descricao, LocalDateTime criadoEm,
                    LocalDateTime sessaoAbertaEm, LocalDateTime sessaoFechaEm, String status) {
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
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getSessaoAbertaEm() { return sessaoAbertaEm; }
    public LocalDateTime getSessaoFechaEm() { return sessaoFechaEm; }
    public String getStatus() { return status; }
}
