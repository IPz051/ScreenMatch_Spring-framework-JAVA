package br.com.alura.Screenmatch.Principal;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import br.com.alura.Screenmatch.model.DadosEpisodio;
import br.com.alura.Screenmatch.model.DadosTemporada;
import br.com.alura.Screenmatch.model.DadosSerie;
import br.com.alura.Screenmatch.service.ConsumoApi;
import br.com.alura.Screenmatch.service.ConverteDados;

public class principal {

    Scanner leitor = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();

    private ConverteDados converteDados = new ConverteDados();

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

    public void exibeMenu() {
        while (true) {
        System.out.println();
        System.out.print("Digite o nome da série (ou 'sair' para finalizar): ");
        String nomeSerie = leitor.nextLine().trim();

        if (nomeSerie.equalsIgnoreCase("sair")) {
        break;
        }

        if (nomeSerie.isBlank()) {
        System.out.println("Nome vazio.");
        continue;
        }

        // Codificar nome da série para evitar problemas de caracteres especiais
        String nomeCodificado = URLEncoder.encode(nomeSerie, StandardCharsets.UTF_8);
        String urlSerie = ENDERECO + nomeCodificado + API_KEY;

        String jsonSerie = consumo.obterDados(urlSerie);
        DadosSerie dadosSerie = converteDados.obterDados(jsonSerie,
        DadosSerie.class);
        System.out.println();
        System.out.println("=== DADOS DA SÉRIE ===");
        System.out.println(dadosSerie);

        // Obter todas as temporadas da série
        Integer totalTemporadas = dadosSerie.temporada();
        if (totalTemporadas == null || totalTemporadas <= 0) {
        System.out.println("Não foi possível obter o total de temporadas.");
        continue;
        }

        // Converter JSON em objeto Java para todas as temporadas
        List<DadosTemporada> temporadas = new ArrayList<>();
        for (int i = 1; i <= totalTemporadas; i++) {
        String urlTemporada = ENDERECO + nomeCodificado + "&season=" + i + API_KEY;
        String jsonTemporada = consumo.obterDados(urlTemporada);
        DadosTemporada dadosTemporada = converteDados.obterDados(jsonTemporada,
        DadosTemporada.class);
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
        })                                                                                      // FlatMap para transformar cada episodio em uma tupla (temporada, episodio)
        .filter(e -> e != null && e.episodio() != null)                                         // Filtrar tuplas com episodio não nulo
        .filter(e -> e.episodio().avaliacao() != null && !e.episodio().avaliacao().equalsIgnoreCase("N/A")) // Filtrar episódios com avaliacao não nula
        .sorted(Comparator.comparingDouble((EpisodioDaTemporada e) -> parseAvaliacao(e.episodio().avaliacao())).reversed()) // Ordenar por avaliacao decrescente
        .limit(5)
        .forEach(e -> {
            Integer temporada = e.temporada();
            Integer episodio = e.episodio().episodio();
            String temporadaLabel = temporada == null ? "??" : String.format("%02d", temporada);  // Formatar número da temporada para 2 dígitos
            String episodioLabel = episodio == null ? "??" : String.format("%02d", episodio);     // Formatar número do episodio para 2 dígitos
            String titulo = e.episodio().titulo() == null ? "(sem título)" : e.episodio().titulo();
            String avaliacao = e.episodio().avaliacao();
            System.out.println("S" + temporadaLabel + "E" + episodioLabel + " - " + titulo + " | IMDb: " + avaliacao);
        });

        System.out.println();
        System.out.println("Todas as temporadas:");
        for (DadosTemporada t : temporadas) {
        System.out.println();
        Integer numeroTemporada = t.temporada();
        System.out.println("=== TEMPORADA " + (numeroTemporada == null ? "?" :
        numeroTemporada) + " ===");

        // Obter todos os episódios da temporada
        List<DadosEpisodio> episodios = t.episodios();
        if (episodios == null || episodios.isEmpty()) {
        System.out.println("Sem episódios.");
        continue;
        }

        // Exibir todos os episódios da temporada
        for (DadosEpisodio e : episodios) {
        Integer numeroEpisodio = e.episodio();
        String epLabel = numeroEpisodio == null ? "??" : String.format("%02d", numeroEpisodio); // Formatar número do episódio para 2 dígitos
        String tituloEpisodio = e.titulo() == null ? "(sem título)" : e.titulo();               // Formatar título do episódio para 2 dígitos
        String avaliacao = e.avaliacao() == null ? "N/A" : e.avaliacao();                       // Formatar avaliacao para 2 dígitos
        System.out.println("E" + epLabel + " - " + tituloEpisodio + " | IMDb: " + avaliacao);   // Exibir informações do episódio

        // List<String> nomes = Arrays.asList("Iggor", "Leila", "Ian", "Algodão", "Nami");

        // nomes.stream() // Converter para fluxo de dados
        //         .sorted() // Ordenar por nome alfabético
        //         .limit(3) // Limitar a 3 primeiros nomes
        //         .filter(n -> n.startsWith("I")) // Filtrar nomes que começam com "I" , com lambda
        //         .map(n -> n.toUpperCase()) // Converter para maiúsculas , com lambda
        //         .forEach(System.out::println);

    }
}
}
}
}
