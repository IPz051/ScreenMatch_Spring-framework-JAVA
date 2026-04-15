package br.com.alura.Screenmatch.model;

import java.util.ArrayList;
import java.util.List;

import br.com.alura.Screenmatch.service.ConsultaChatGPT;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "series")

public class Serie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //
    private long id;

    @Column(unique = true)
    private String titulo;
    private Integer temporada;
    private double avaliacao;
    @Enumerated(EnumType.STRING)
    @Column(name = "genero")
    private CategoriaEnum genero;
    private String atores;
    private String poster;
    private String sinopse;

    @Transient
    private List <Episodio> episodios = new ArrayList<>();

    
    public Serie() {
    }
    
    public Serie(DadosSerie dadosSerie) {
        this.titulo = dadosSerie.titulo();
        this.temporada = dadosSerie.temporada();
        this.avaliacao = parseAvaliacao(dadosSerie.avaliacao());
        this.genero = CategoriaEnum.fromString(dadosSerie.genero().split(",")[0]);
        this.atores = dadosSerie.atores();
        this.poster = dadosSerie.poster();
        this.sinopse = ConsultaChatGPT.obterTraducao(dadosSerie.sinopse()).trim();
    }
    


    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getTemporada() {
        return temporada;
    }
    public void setTemporada(Integer temporada) {
        this.temporada = temporada;
    }

    public double getAvaliacao() {
        return avaliacao;
    }
    public void setAvaliacao(double avaliacao) {
        this.avaliacao = avaliacao;
    }

    public CategoriaEnum getGenero() {
        return genero;
    }
    public void setGenero(CategoriaEnum genero) {
        this.genero = genero;
    }

    public String getAtores() {
        return atores;
    }
    public void setAtores(String atores) {
        this.atores = atores;
    }

    public String getPoster() {
        return poster;
    }
    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getSinopse() {
        return sinopse;
    }
    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    @Override
    public String toString() {
        return "Serie{" +
                "titulo='" + titulo + '\'' +
                ", temporada=" + temporada +
                ", avaliacao=" + avaliacao +
                ", genero=" + genero +
                ", atores='" + atores + '\'' +
                ", poster='" + poster + '\'' +
                ", sinopse='" + sinopse + '\'' +
                '}';
    }

    private double parseAvaliacao(String avaliacao) {
        if (avaliacao == null || avaliacao.isBlank() || avaliacao.equalsIgnoreCase("N/A")) {
            return 0;
        }
        try {
            return Double.parseDouble(avaliacao);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
