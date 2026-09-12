package br.com.fiap.streamfiap.model;

import jakarta.persistence.Entity;

@Entity
public class Serie extends Conteudo implements Promocionavel {

    private int numeroTemporadas;

    public Serie() {
    }

    // cria a série com os dados recebidos
    public Serie(String titulo, String categoria, int duracaoMinutos, int classificacaoEtaria,
                 boolean disponivel, int numeroTemporadas) {
        super(titulo, categoria, duracaoMinutos, classificacaoEtaria, disponivel);
        this.numeroTemporadas = numeroTemporadas;
    }

    // preço da série: 4.90 por temporada
    @Override
    public double calcularPrecoAluguel() {
        double preco = 4.90 * numeroTemporadas;
        return Math.round(preco * 100.0) / 100.0;
    }

    @Override
    public double aplicarPromocao(double preco) {
        return Math.round(preco * 0.8 * 100.0) / 100.0;
    }

    public int getNumeroTemporadas() { return numeroTemporadas; }
    public void setNumeroTemporadas(int numeroTemporadas) { this.numeroTemporadas = numeroTemporadas; }
}
