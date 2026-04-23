package br.com.alura.Screenmatch.Principal;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;

import br.com.alura.Screenmatch.Repository.EpisodioRepository;
import br.com.alura.Screenmatch.Repository.SerieRepository;
import br.com.alura.Screenmatch.model.CategoriaEnum;
import br.com.alura.Screenmatch.model.DadosEpisodio;
import br.com.alura.Screenmatch.model.DadosTemporada;
import br.com.alura.Screenmatch.model.DadosSerie;
import br.com.alura.Screenmatch.model.Episodio;
import br.com.alura.Screenmatch.model.Serie;
import br.com.alura.Screenmatch.service.ConsumoApi;
import br.com.alura.Screenmatch.service.ConverteDados;

public class principal {

    Scanner leitor = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private final SerieRepository repository;
    private final EpisodioRepository episodioRepository;
    private ConverteDados converteDados = new ConverteDados();
    private List<Serie> seriesBuscadas = new ArrayList<>();

    public principal(SerieRepository repository, EpisodioRepository episodioRepository) {
        this.repository = repository;
        this.episodioRepository = episodioRepository;
    }

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
            System.out.println("3 - Listar episódios de uma série");
            System.out.println("4 - Buscar séries por ator");
            System.out.println("5 - TOP 5 séries");
            System.out.println("6 - Listar séries por categoria");
            System.out.println("7 - Séries por temporadas e avaliação");
            System.out.println("8 - Episódios por trecho do título");
            System.out.println("9 - TOP episódios por série");
            System.out.println("10 - Séries por data de lançamento");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");
            String opcao = leitor.nextLine().trim();

            
            if (opcao.equals("1")) {
                buscarSerie();
                continue;
            }

            if (opcao.equals("2")) {
                listarSeriesBuscadas();
                continue;
            }

            if (opcao.equals("3")) {
                listarEpisodiosPorSerieNoBanco();
                continue;
            }
            
            if (opcao.equals("4")) {
                listarSeriesPorAtorNoBanco();
                continue;
            }
            
            if (opcao.equals("5")) {
                listarTop5Series();
                continue;
            }

            if (opcao.equals("6")) {
                listarSeriesPorCategoria();
                continue;
            }

            if (opcao.equals("7")) {
                listarSeriesPorTemporadaEAvaliacao();
                continue;
            }

            if (opcao.equals("8")) {
                listarEpisodiosPorTrecho();
                continue;
            }

            if (opcao.equals("9")) {
                listarTopEpisodiosPorSerie();
                continue;
            }

            if (opcao.equals("10")) {
                listarSeriesPorDataLancamento();
                continue;
            }

            if (opcao.equals("0")) {
                break;
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

        exibirDadosSerie(dadosSerie);

        Serie serie = repository.findByTituloIgnoreCase(dadosSerie.titulo())
                .orElseGet(() -> new Serie(dadosSerie));
        boolean novaSerie = serie.getId() == 0;
        if (novaSerie) {
            repository.save(serie);
        }
        seriesBuscadas.add(serie);

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

        if (episodioRepository.countBySerieId(serie.getId()) == 0) {
            salvarEpisodiosDaSerieNoBanco(serie, temporadas);
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
                
                // Buscar episódio por título
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

        seriesBuscadas = repository.findAll();

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

    private void listarEpisodiosPorSerieNoBanco() {
        System.out.println();
        System.out.println("=== EPISÓDIOS (BANCO) ===");
        System.out.print("Digite o título exato da série: ");
        String tituloSerie = leitor.nextLine().trim();

        if (tituloSerie.isBlank()) {
            System.out.println("Título vazio.");
            return;
        }

        var serieOpt = repository.findByTituloIgnoreCase(tituloSerie);
        if (serieOpt.isEmpty()) {
            System.out.println("Série não encontrada no banco.");
            return;
        }

        Serie serie = serieOpt.get();
        if (episodioRepository.countBySerieId(serie.getId()) == 0) {
            carregarEpisodiosDaSerieNoBanco(serie);
        }

        List<Episodio> episodios = episodioRepository.findBySerieIdOrderByTemporadaAscNumeroEpisodioAsc(serie.getId());

        if (episodios.isEmpty()) {
            System.out.println("Nenhum episódio salvo para essa série.");
            return;
        }

        for (Episodio ep : episodios) {
            Integer temporada = ep.getTemporada();
            Integer numero = ep.getNumeroEpisodio();
            String temporadaLabel = temporada == null ? "??" : String.format("%02d", temporada);
            String episodioLabel = numero == null ? "??" : String.format("%02d", numero);
            String titulo = ep.getTitulo() == null ? "(sem título)" : ep.getTitulo();
            String avaliacao = ep.getAvaliacao() <= 0 ? "N/A" : String.valueOf(ep.getAvaliacao());
            System.out.println("S" + temporadaLabel + "E" + episodioLabel + " - " + titulo + " | IMDb: " + avaliacao);
        }
    }

    private void listarSeriesPorAtorNoBanco() {
        System.out.println();
        System.out.println("=== SÉRIES POR ATOR (BANCO) ===");
        System.out.print("Digite o nome do ator: ");
        String nomeAtor = leitor.nextLine().trim();

        if (nomeAtor.isBlank()) {
            System.out.println("Nome vazio.");
            return;
        }

        List<Serie> series = repository.findByAtoresContainingIgnoreCase(nomeAtor);
        if (series.isEmpty()) {
            System.out.println("Nenhuma série encontrada para esse ator no banco.");
            return;
        }

        for (int i = 0; i < series.size(); i++) {
            Serie serie = series.get(i);
            String nota = serie.getAvaliacao() <= 0 ? "N/A"
                    : String.format(java.util.Locale.US, "%.1f", serie.getAvaliacao());
            System.out.println((i + 1) + ". " + serie.getTitulo()
                    + " | Temporadas: " + serie.getTemporada()
                    + " | IMDb: " + nota);
        }
    }

    private void carregarEpisodiosDaSerieNoBanco(Serie serie) {
        String nomeCodificado = URLEncoder.encode(serie.getTitulo(), StandardCharsets.UTF_8);
        String urlSerie = ENDERECO + nomeCodificado + API_KEY;

        String jsonSerie = consumo.obterDados(urlSerie);
        DadosSerie dadosSerie = converteDados.obterDados(jsonSerie, DadosSerie.class);
        Integer totalTemporadas = dadosSerie.temporada();
        if (totalTemporadas == null || totalTemporadas <= 0) {
            return;
        }

        List<DadosTemporada> temporadas = new ArrayList<>();
        for (int i = 1; i <= totalTemporadas; i++) {
            String urlTemporada = ENDERECO + nomeCodificado + "&season=" + i + API_KEY;
            String jsonTemporada = consumo.obterDados(urlTemporada);
            DadosTemporada dadosTemporada = converteDados.obterDados(jsonTemporada, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }

        salvarEpisodiosDaSerieNoBanco(serie, temporadas);
    }

    private void salvarEpisodiosDaSerieNoBanco(Serie serie, List<DadosTemporada> temporadas) {
        episodioRepository.deleteBySerieId(serie.getId());

        List<Episodio> episodiosParaSalvar = new ArrayList<>();
        for (DadosTemporada t : temporadas) {
            List<DadosEpisodio> eps = t.episodios();
            if (eps == null || eps.isEmpty()) {
                continue;
            }
            for (DadosEpisodio e : eps) {
                Episodio episodio = new Episodio(t.temporada(), e);
                episodio.setSerie(serie);
                episodiosParaSalvar.add(episodio);
            }
        }
        if (!episodiosParaSalvar.isEmpty()) {
            episodioRepository.saveAll(episodiosParaSalvar);
        }
    }
    private void listarTop5Series() {
        System.out.println();
        System.out.println("=== TOP 5 SÉRIES ===");
        List<Serie> series = repository.findTop5ByOrderByAvaliacaoDesc();
        if (series.isEmpty()) {
            System.out.println("Nenhuma série encontrada no banco.");
            return;
        }
        for (int i = 0; i < series.size(); i++) {
            Serie serie = series.get(i);
            String nota = serie.getAvaliacao() <= 0 ? "N/A"
                    : String.format(java.util.Locale.US, "%.1f", serie.getAvaliacao());
            System.out.println((i + 1) + ". " + serie.getTitulo()
                    + " | Temporadas: " + serie.getTemporada()
                    + " | IMDb: " + nota);
        }
    }
    private void listarSeriesPorCategoria() {
        System.out.println();
        System.out.println("=== CATEGORIAS ===");
        CategoriaEnum[] categorias = CategoriaEnum.values();
        for (int i = 0; i < categorias.length; i++) {
            System.out.println((i + 1) + " - " + categorias[i]);
        }
        System.out.print("Digite o número ou o nome da categoria (ex: 1 ou Acao): ");

        String entrada = leitor.nextLine().trim();

        CategoriaEnum categoria = null;
        if (entrada.matches("\\d+")) {
            int indice = Integer.parseInt(entrada);
            if (indice >= 1 && indice <= categorias.length) {
                categoria = categorias[indice - 1];
            }
        }
        try {
            if (categoria == null) {
                categoria = CategoriaEnum.fromPortugues(entrada);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Categoria inválida.");
            return;
        }

        List<Serie> series = repository.findByGenero(categoria);
        System.out.println("=== SÉRIES POR CATEGORIA ===");
        if (series.isEmpty()) {
            System.out.println("Nenhuma série encontrada para essa categoria.");
            return;
        }
        for (int i = 0; i < series.size(); i++) {
            Serie serie = series.get(i);
            String nota = serie.getAvaliacao() <= 0 ? "N/A"
                    : String.format(java.util.Locale.US, "%.1f", serie.getAvaliacao());
            System.out.println((i + 1) + ". " + serie.getTitulo()
                    + " | Temporadas: " + serie.getTemporada()
                    + " | IMDb: " + nota);
        }
    }

    private void listarSeriesPorTemporadaEAvaliacao() {
        System.out.println();
        System.out.println("=== SÉRIES POR TEMPORADAS E AVALIAÇÃO ===");
        System.out.print("Temporadas mínimas: ");
        String temporadasEntrada = leitor.nextLine().trim();
        System.out.print("Avaliação mínima (ex: 7.5): ");
        String avaliacaoEntrada = leitor.nextLine().trim();

        Integer temporadas;
        Double avaliacao;
        try {
            temporadas = Integer.parseInt(temporadasEntrada);
            avaliacao = Double.parseDouble(avaliacaoEntrada);
        } catch (NumberFormatException e) {
            System.out.println("Valores inválidos.");
            return;
        }

        List<Serie> series = repository.seriesPorTemporadaEAvaliacao(temporadas, avaliacao);
        if (series.isEmpty()) {
            System.out.println("Nenhuma série encontrada para esses filtros.");
            return;
        }

        for (int i = 0; i < series.size(); i++) {
            Serie serie = series.get(i);
            String nota = serie.getAvaliacao() <= 0 ? "N/A"
                    : String.format(java.util.Locale.US, "%.1f", serie.getAvaliacao());
            System.out.println((i + 1) + ". " + serie.getTitulo()
                    + " | Temporadas: " + serie.getTemporada()
                    + " | IMDb: " + nota);
        }
    }

    private void listarEpisodiosPorTrecho() {
        System.out.println();
        System.out.println("=== EPISÓDIOS POR TRECHO ===");
        System.out.print("Digite um trecho do título do episódio: ");
        String trecho = leitor.nextLine().trim();

        if (trecho.isBlank()) {
            System.out.println("Trecho vazio.");
            return;
        }

        List<Episodio> episodios = episodioRepository.episodiosPorTrecho(trecho);
        if (episodios.isEmpty()) {
            System.out.println("Nenhum episódio encontrado.");
            return;
        }

        for (Episodio ep : episodios) {
            String tituloSerie = ep.getSerie() == null || ep.getSerie().getTitulo() == null ? "(sem série)"
                    : ep.getSerie().getTitulo();
            Integer temporada = ep.getTemporada();
            Integer numero = ep.getNumeroEpisodio();
            String temporadaLabel = temporada == null ? "??" : String.format("%02d", temporada);
            String episodioLabel = numero == null ? "??" : String.format("%02d", numero);
            String titulo = ep.getTitulo() == null ? "(sem título)" : ep.getTitulo();
            String avaliacao = ep.getAvaliacao() <= 0 ? "N/A" : String.valueOf(ep.getAvaliacao());
            System.out.println(tituloSerie + " - S" + temporadaLabel + "E" + episodioLabel + " - " + titulo + " | IMDb: "
                    + avaliacao);
        }
    }

    private void listarTopEpisodiosPorSerie() {
        System.out.println();
        System.out.println("=== TOP EPISÓDIOS POR SÉRIE ===");
        System.out.print("Digite o título exato da série: ");
        String tituloSerie = leitor.nextLine().trim();

        if (tituloSerie.isBlank()) {
            System.out.println("Título vazio.");
            return;
        }

        var serieOpt = repository.findByTituloIgnoreCase(tituloSerie);
        if (serieOpt.isEmpty()) {
            System.out.println("Série não encontrada no banco.");
            return;
        }

        Serie serie = serieOpt.get();
        if (episodioRepository.countBySerieId(serie.getId()) == 0) {
            carregarEpisodiosDaSerieNoBanco(serie);
        }

        List<Episodio> episodios = episodioRepository.topEpisodiosPorSerie(serie.getTitulo(), PageRequest.of(0, 5));
        if (episodios.isEmpty()) {
            System.out.println("Nenhum episódio com avaliação encontrada para essa série.");
            return;
        }

        for (int i = 0; i < episodios.size(); i++) {
            Episodio ep = episodios.get(i);
            Integer temporada = ep.getTemporada();
            Integer numero = ep.getNumeroEpisodio();
            String temporadaLabel = temporada == null ? "??" : String.format("%02d", temporada);
            String episodioLabel = numero == null ? "??" : String.format("%02d", numero);
            String titulo = ep.getTitulo() == null ? "(sem título)" : ep.getTitulo();
            String avaliacao = ep.getAvaliacao() <= 0 ? "N/A" : String.valueOf(ep.getAvaliacao());
            System.out.println((i + 1) + ". S" + temporadaLabel + "E" + episodioLabel + " - " + titulo + " | IMDb: "
                    + avaliacao);
        }
    }

    private void listarSeriesPorDataLancamento() {
        System.out.println();
        System.out.println("=== SÉRIES POR DATA DE LANÇAMENTO ===");
        System.out.print("Data inicial (AAAA-MM-DD): ");
        String inicioEntrada = leitor.nextLine().trim();
        System.out.print("Data final (AAAA-MM-DD): ");
        String fimEntrada = leitor.nextLine().trim();

        LocalDate inicio;
        LocalDate fim;
        try {
            inicio = LocalDate.parse(inicioEntrada);
            fim = LocalDate.parse(fimEntrada);
        } catch (DateTimeParseException e) {
            System.out.println("Data inválida. Use o formato AAAA-MM-DD.");
            return;
        }

        List<Serie> series = repository.seriesPorDataLancamento(inicio, fim);
        if (series.isEmpty()) {
            System.out.println("Nenhuma série encontrada nesse período.");
            return;
        }

        for (int i = 0; i < series.size(); i++) {
            Serie serie = series.get(i);
            String nota = serie.getAvaliacao() <= 0 ? "N/A"
                    : String.format(java.util.Locale.US, "%.1f", serie.getAvaliacao());
            String data = serie.getDataLancamento() == null ? "N/A" : serie.getDataLancamento().toString();
            System.out.println((i + 1) + ". " + serie.getTitulo()
                    + " | Lançamento: " + data
                    + " | Temporadas: " + serie.getTemporada()
                    + " | IMDb: " + nota);
        }
    }
}
