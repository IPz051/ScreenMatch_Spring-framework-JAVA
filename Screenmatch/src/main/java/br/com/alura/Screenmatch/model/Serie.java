package br.com.alura.Screenmatch.model;

public class Serie {

    private String titulo;
    private Integer temporada;
    private double avaliacao;
    private CategoriaEnum genero;
    private String atores;
    private String poster;
    private String sinopse;

    public Serie(DadosSerie dadosSerie) {
        this.titulo = dadosSerie.titulo();
        this.temporada = dadosSerie.temporada();
        this.avaliacao = parseAvaliacao(dadosSerie.avaliacao());
        this.genero = CategoriaEnum.fromString(dadosSerie.genero().split(",")[0]);
        this.atores = dadosSerie.atores();
        this.poster = dadosSerie.poster();
        this.sinopse = dadosSerie.sinopse();
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
