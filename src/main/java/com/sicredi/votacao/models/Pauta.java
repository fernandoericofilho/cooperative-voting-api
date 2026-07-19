package com.sicredi.votacao.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "pauta")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pauta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 2000)
    private String descricao;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Column(name = "sessao_aberta_em")
    private LocalDateTime sessaoAbertaEm;

    @Column(name = "sessao_fecha_em")
    private LocalDateTime sessaoFechaEm;

    public Pauta(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.criadaEm = LocalDateTime.now();
    }

    public boolean sessaoFoiAberta() {
        return sessaoAbertaEm != null;
    }

    public boolean sessaoEstaAberta() {
        return sessaoFoiAberta() && LocalDateTime.now().isBefore(sessaoFechaEm);
    }

    public boolean sessaoEstaEncerrada() {
        return sessaoFoiAberta() && !sessaoEstaAberta();
    }

    public void abrirSessao(long duracaoSegundos) {
        this.sessaoAbertaEm = LocalDateTime.now();
        this.sessaoFechaEm = this.sessaoAbertaEm.plusSeconds(duracaoSegundos);
    }
}
