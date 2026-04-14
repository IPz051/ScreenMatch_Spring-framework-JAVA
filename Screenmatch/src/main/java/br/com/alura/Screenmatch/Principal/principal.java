package br.com.alura.Screenmatch.Principal;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.com.alura.Screenmatch.model.DadosEpisodio;
import br.com.alura.Screenmatch.model.DadosTemporada;
import br.com.alura.Screenmatch.model.DadosSerie;
import br.com.alura.Screenmatch.model.Serie;
import br.com.alura.Screenmatch.service.ConsumoApi;
import br.com.alura.Screenmatch.service.ConverteDados;

public class principal {

    Scanner leitor = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();

    private ConverteDados converteDados = new ConverteDados();
    private List<Serie> seriesBuscadas = new ArrayList<>();

    // Constantes que não vão sofrer alterações (final) ,
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=63858cd6";

    private double parseAvaliacao(String avaliacao) {
        if (avaliacao == null) {
            return -1;
        }
        try {
            return Double.parseDouble(avaliacao);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    // Método para tratar os valores nulos ou vazios
    private String textoOuPadrao(String valor) {
        return (valor == null || valor.isBlank()) ? "N/A" : valor;
    }

    // Método para quebrar o texto em linhas com largura máxima
    private String quebrarTexto(String texto, int largura) {
        String conteudo = textoOuPadrao(texto);
        if (conteudo.length() <= largura) {
            return conteudo;
        }

        String[] palavras = conteudo.split("\\s+");
        StringBuilder resultado = new StringBuilder();
        StringBuilder linhaAtual = new StringBuilder();

        for (String palavra : palavras) {
            if (linhaAtual.length() == 0) {
                linhaAtual.append(palavra);
                continue;
            }

            if (linhaAtual.length() + palavra.length() + 1 > largura) {
                resultado.append(linhaAtual).append(System.lineSeparator());
                linhaAtual.setLength(0);
                linhaAtual.append(palavra);
            } else {
                linhaAtual.append(" ").append(palavra);
            }
        }

        if (linhaAtual.length() > 0) {
            resultado.append(linhaAtual);
        }

        return resultado.toString();
    }

    // Método para exibir os dados da série , com todos os campos do objeto Serie
       private void exibirDadosSerie(DadosSerie dadosSerie) {
        String fichaSerie = """
                === DADOS DA SERIE ===
                Titulo: %s
                Temporadas: %s
                Avaliacao IMDb: %s
                Votos: %s
                Genero: %s
                Atores: %s
                Poster: %s
                Sinopse:
                %s
                """.formatted(
                textoOuPadrao(dadosSerie.titulo()),
                dadosSerie.temporada() == null ? "N/A" : dadosSerie.temporada(),
                textoOuPadrao(dadosSerie.avaliacao()),
                textoOuPadrao(dadosSerie.votos()),
                textoOuPadrao(dadosSerie.genero()),
                textoOuPadrao(dadosSerie.atores()),
                textoOuPadrao(dadosSerie.poster()),
                quebrarTexto(dadosSerie.sinopse(), 80));

        System.out.println();
        System.out.println(fichaSerie);
    }

    // Método para exibir o menu principal
    public void exibeMenu() {
        while (true) {
            System.out.println();
            System.out.println("=== MENU SCREENMATCH ===");
            System.out.println("1 - Buscar série");
            System.out.println("2 - Listar séries buscadas");
            System.out.println("3 - Sair");
            System.out.print("Escolha uma opção: ");
            String opcao = leitor.nextLine().trim();

            if (opcao.equals("3")) {
                break;
            }

            if (opcao.equals("1")) {
                buscarSerie();
                continue;
            }

            if (opcao.equals("2")) {
                listarSeriesBuscadas();
                continue;
            }

            System.out.println("Opção inválida.");
        }
    }

    private void buscarSerie() {
        System.out.println();
        System.out.print("Digite o nome da série: ");
        String nomeSerie = leitor.nextLine().trim();

        if (nomeSerie.isBlank()) {
            System.out.println("Nome vazio.");
            return;
        }

        String nomeCodificado = URLEncoder.encode(nomeSerie, StandardCharsets.UTF_8);
        String urlSerie = ENDERECO + nomeCodificado + API_KEY;

        String jsonSerie = consumo.obterDados(urlSerie);
        DadosSerie dadosSerie = converteDados.obterDados(jsonSerie, DadosSerie.class);
        if (dadosSerie.titulo() == null || dadosSerie.titulo().isBlank()) {
            System.out.println("Série não encontrada.");
            return;
        }

        seriesBuscadas.add(new Serie(dadosSerie));
        exibirDadosSerie(dadosSerie);

        Integer totalTemporadas = dadosSerie.temporada();
        if (totalTemporadas == null || totalTemporadas <= 0) {
            System.out.println("Não foi possível obter o total de temporadas.");
            return;
        }

        List<DadosTemporada> temporadas = new ArrayList<>();
        for (int i = 1; i <= totalTemporadas; i++) {
            String urlTemporada = ENDERECO + nomeCodificado + "&season=" + i + API_KEY;
            String jsonTemporada = consumo.obterDados(urlTemporada);
            DadosTemporada dadosTemporada = converteDados.obterDados(jsonTemporada, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }

        record EpisodioDaTemporada(Integer temporada, DadosEpisodio episodio) {
        }

        System.out.println("\n=== 5 EPISÓDIOS COM MAIOR AVALIAÇÃO IMDB ===");
        temporadas.stream()
                .flatMap(temporada -> {
                    List<DadosEpisodio> eps = temporada.episodios();
                    if (eps == null || eps.isEmpty()) {
                        return Stream.<EpisodioDaTemporada>empty();
                    }
                    return eps.stream().map(e -> new EpisodioDaTemporada(temporada.temporada(), e));
                })
                .filter(e -> e != null && e.episodio() != null)
                .filter(e -> e.episodio().avaliacao() != null && !e.episodio().avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator
                        .comparingDouble((EpisodioDaTemporada e) -> parseAvaliacao(e.episodio().avaliacao()))
                        .reversed())
                .limit(5)
                .forEach(e -> {
                    Integer temporada = e.temporada();
                    Integer episodio = e.episodio().episodio();
                    String temporadaLabel = temporada == null ? "??" : String.format("%02d", temporada);
                    String episodioLabel = episodio == null ? "??" : String.format("%02d", episodio);
                    String titulo = e.episodio().titulo() == null ? "(sem título)" : e.episodio().titulo();
                    String avaliacao = e.episodio().avaliacao();
                    System.out.println(
                            "S" + temporadaLabel + "E" + episodioLabel + " - " + titulo + " | IMDb: " + avaliacao);
                });

        System.out.print("\nDigite parte do título do episódio para buscar (ou Enter para pular): ");
        String termoBusca = leitor.nextLine().trim();
        if (!termoBusca.isEmpty()) {
            temporadas.stream()
                    .flatMap(temporada -> {
                        List<DadosEpisodio> eps = temporada.episodios();
                        if (eps == null || eps.isEmpty()) {
                            return Stream.empty();
                        }
                        return eps.stream()
                                .filter(ep -> ep.titulo() != null
                                        && ep.titulo().toLowerCase().contains(termoBusca.toLowerCase()))
                                .map(ep -> new EpisodioDaTemporada(temporada.temporada(), ep));
                    })
                    .findFirst()
                    .ifPresent(e -> {
                        Integer tnum = e.temporada();
                        Integer enumero = e.episodio().episodio();
                        String tLabel = tnum == null ? "??" : String.format("%02d", tnum);
                        String eLabel = enumero == null ? "??" : String.format("%02d", enumero);
                        String titulo = e.episodio().titulo() == null ? "(sem título)" : e.episodio().titulo();
                        String avaliacao = e.episodio().avaliacao();
                        System.out.println(
                                "Encontrado: S" + tLabel + "E" + eLabel + " - " + titulo + " | IMDb: " + avaliacao);
                    });
        }

        System.out.println();
        System.out.println("Todas as temporadas:");
        for (DadosTemporada t : temporadas) {
            System.out.println();
            Integer numeroTemporada = t.temporada();
            System.out.println("=== TEMPORADA " + (numeroTemporada == null ? "?" : numeroTemporada) + " ===");

            List<DadosEpisodio> episodios = t.episodios();
            if (episodios == null || episodios.isEmpty()) {
                System.out.println("Sem episódios.");
                continue;
            }

            for (DadosEpisodio e : episodios) {
                Integer numeroEpisodio = e.episodio();
                String epLabel = numeroEpisodio == null ? "??" : String.format("%02d", numeroEpisodio);
                String tituloEpisodio = e.titulo() == null ? "(sem título)" : e.titulo();
                String avaliacao = e.avaliacao() == null ? "N/A" : e.avaliacao();
                System.out.println("E" + epLabel + " - " + tituloEpisodio + " | IMDb: " + avaliacao);
            }

            double mediaTemporada = episodios.stream()
                    .mapToDouble(ep -> parseAvaliacao(ep.avaliacao()))
                    .filter(nota -> nota > 0)
                    .average()
                    .orElse(-1);

            if (mediaTemporada < 0) {
                System.out.println("\nMédia da temporada: N/A");
            } else {
                System.out.println(
                        "\nMédia da temporada: " + String.format(java.util.Locale.US, "%.3f", mediaTemporada));
            }

            DoubleSummaryStatistics estatistica = episodios.stream()
                    .filter(ep -> ep.avaliacao() != null)
                    .collect(Collectors.summarizingDouble(ep -> parseAvaliacao(ep.avaliacao())));
            System.out.println("Melhor Episódio: " + estatistica.getMax());
            System.out.println("Pior Episódio: " + estatistica.getMin());
            System.out.println("Contagem: " + estatistica.getCount());
        }
    }

    private void listarSeriesBuscadas() {
        System.out.println();
        System.out.println("=== SÉRIES BUSCADAS ===");

        if (seriesBuscadas.isEmpty()) {
            System.out.println("Nenhuma série foi buscada ainda.");
            return;
        }

        for (int i = 0; i < seriesBuscadas.size(); i++) {
            Serie serie = seriesBuscadas.get(i);
            String nota = serie.getAvaliacao() <= 0 ? "N/A"
                    : String.format(java.util.Locale.US, "%.1f", serie.getAvaliacao());
            System.out.println((i + 1) + ". " + serie.getTitulo()
                    + " | Temporadas: " + serie.getTemporada()
                    + " | IMDb: " + nota);
        }
    }
}
