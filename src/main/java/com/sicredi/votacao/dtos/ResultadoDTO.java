package com.sicredi.votacao.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultadoDTO {
    private Long pautaId;
    private Integer totalSim;
    private Integer totalNao;
    private String resultado;
    private String status;

    public ResultadoDTO(Long pautaId, Integer totalSim, Integer totalNao, String resultado, String status) {
        this.pautaId = pautaId;
        this.totalSim = totalSim;
        this.totalNao = totalNao;
        this.resultado = resultado;
        this.status = status;
    }

    // Getters
    public Long getPautaId() { return pautaId; }
    public Integer getTotalSim() { return totalSim; }
    public Integer getTotalNao() { return totalNao; }
    public String getResultado() { return resultado; }
    public String getStatus() { return status; }
}
