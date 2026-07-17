package com.sicredi.votacao.pauta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "pauta")
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

    protected Pauta() {
    }

    public Pauta(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.criadaEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public LocalDateTime getSessaoAbertaEm() {
        return sessaoAbertaEm;
    }

    public LocalDateTime getSessaoFechaEm() {
        return sessaoFechaEm;
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
}
