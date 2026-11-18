package br.com.sebrae.atividade_produtos.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "movimentacoes")
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    /** Entrada/saída: quantidade movimentada. Ajuste: contagem física informada. */
    @Column(nullable = false)
    private int quantidade;

    @Column(name = "saldo_anterior", nullable = false)
    private int saldoAnterior;

    @Column(name = "saldo_posterior", nullable = false)
    private int saldoPosterior;

    @Column(length = 255)
    private String motivo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    public Movimentacao() {
    }

    public Movimentacao(Produto produto, TipoMovimentacao tipo, int quantidade,
                        int saldoAnterior, int saldoPosterior, String motivo) {
        this.produto = produto;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.saldoAnterior = saldoAnterior;
        this.saldoPosterior = saldoPosterior;
        this.motivo = motivo;
    }

    /** Variação líquida do saldo (positiva ou negativa). */
    public int getVariacao() {
        return saldoPosterior - saldoAnterior;
    }

    public Long getId() { return id; }
    public Produto getProduto() { return produto; }
    public TipoMovimentacao getTipo() { return tipo; }
    public int getQuantidade() { return quantidade; }
    public int getSaldoAnterior() { return saldoAnterior; }
    public int getSaldoPosterior() { return saldoPosterior; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getDataHora() { return dataHora; }
}
