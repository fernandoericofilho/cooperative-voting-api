package com.sicredi.votacao.models;

import com.sicredi.votacao.enums.OpcaoVoto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "voto", uniqueConstraints = @UniqueConstraint(name = "uk_voto_pauta_cpf", columnNames = {"pauta_id", "cpf_associado"}))
@Getter
@Setter
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pauta_id", nullable = false)
    private Long pautaId;

    @Column(name = "cpf_associado", nullable = false, length = 11, columnDefinition = "CHAR(11)")
    private String cpfAssociado;

    @Enumerated(EnumType.STRING)
    @Column(name = "voto", nullable = false, length = 3)
    private OpcaoVoto voto;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    protected Voto() {
    }

    public Voto(Long pautaId, String cpfAssociado, OpcaoVoto voto) {
        this.pautaId = pautaId;
        this.cpfAssociado = cpfAssociado;
        this.voto = voto;
        this.criadoEm = LocalDateTime.now();
    }
}
