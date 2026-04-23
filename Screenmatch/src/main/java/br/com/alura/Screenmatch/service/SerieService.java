package br.com.alura.Screenmatch.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.alura.Screenmatch.DTO.serieDTO;
import br.com.alura.Screenmatch.Repository.EpisodioRepository;
import br.com.alura.Screenmatch.Repository.SerieRepository;
import br.com.alura.Screenmatch.model.DadosEpisodio;
import br.com.alura.Screenmatch.model.DadosSerie;
import br.com.alura.Screenmatch.model.DadosTemporada;
import br.com.alura.Screenmatch.model.Episodio;
import br.com.alura.Screenmatch.model.Serie;

@Service
public class SerieService {

    @Autowired
    private SerieRepository serieRepository;

    @Autowired
    private EpisodioRepository episodioRepository;

    private final ConsumoApi consumoApi = new ConsumoApi();
    private final ConverteDados converteDadosJson = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=63858cd6";

    public List<serieDTO> obterTodasAsSeries () {
        return converteDados(serieRepository.findAll());
    }

    public List<serieDTO> obterTop5Series () {
        return converteDados(serieRepository.findTop5ByOrderByAvaliacaoDesc());
    }

    public serieDTO adicionarSerie(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Título vazio");
        }

        String nomeCodificado = URLEncoder.encode(titulo.trim(), StandardCharsets.UTF_8);
        String urlSerie = ENDERECO + nomeCodificado + "&type=series" + API_KEY;

        String jsonSerie = consumoApi.obterDados(urlSerie);
        DadosSerie dadosSerie = converteDadosJson.obterDados(jsonSerie, DadosSerie.class);
        if (dadosSerie.titulo() == null || dadosSerie.titulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Série não encontrada");
        }

        Serie serie = serieRepository.findByTituloIgnoreCase(dadosSerie.titulo()).orElse(null);
        if (serie == null) {
            serie = new Serie(dadosSerie);
            serieRepository.save(serie);
        } else {
            Serie atualizada = new Serie(dadosSerie);
            serie.setTemporada(atualizada.getTemporada());
            serie.setAvaliacao(atualizada.getAvaliacao());
            serie.setGenero(atualizada.getGenero());
            serie.setAtores(atualizada.getAtores());
            serie.setPoster(atualizada.getPoster());
            serie.setSinopse(atualizada.getSinopse());
            serie.setDataLancamento(atualizada.getDataLancamento());
            serieRepository.save(serie);
        }

        Integer totalTemporadas = dadosSerie.temporada();
        if (totalTemporadas != null && totalTemporadas > 0 && episodioRepository.countBySerieId(serie.getId()) == 0) {
            List<DadosTemporada> temporadas = new ArrayList<>();
            for (int i = 1; i <= totalTemporadas; i++) {
                String urlTemporada = ENDERECO + nomeCodificado + "&season=" + i + API_KEY;
                String jsonTemporada = consumoApi.obterDados(urlTemporada);
                DadosTemporada dadosTemporada = converteDadosJson.obterDados(jsonTemporada, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            salvarEpisodiosDaSerieNoBanco(serie, temporadas);
        }

        return converteDados(List.of(serie)).get(0);
    }

    private List<serieDTO> converteDados(List<Serie> series){
        return series.stream()
                .map(serie -> {
                    return new serieDTO(
                            serie.getId(),
                            serie.getTitulo(),
                            serie.getTemporada(),
                            serie.getAvaliacao(),
                            serie.getGenero(),
                            serie.getAtores(),
                            serie.getPoster(),
                            serie.getSinopse(),
                            serie.getDataLancamento());
                })
                .toList();
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

    public List<serieDTO> obterLancamentos () {
        return converteDados(serieRepository.seriesPorDataLancamentoDesc());
    }
    public serieDTO obterSerie(Long id){
        return serieRepository.findById(id)
                .map(serie -> converteDados(List.of(serie)).get(0))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Série não encontrada"));
    }

    public List<Episodio> obterEpisodiosDaSerie(Long id) {
        Serie serie = serieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Série não encontrada"));

        if (episodioRepository.countBySerieId(serie.getId()) == 0) {
            Integer totalTemporadas = serie.getTemporada();
            if (totalTemporadas != null && totalTemporadas > 0) {
                String nomeCodificado = URLEncoder.encode(serie.getTitulo().trim(), StandardCharsets.UTF_8);
                List<DadosTemporada> temporadas = new ArrayList<>();
                for (int i = 1; i <= totalTemporadas; i++) {
                    String urlTemporada = ENDERECO + nomeCodificado + "&season=" + i + API_KEY;
                    String jsonTemporada = consumoApi.obterDados(urlTemporada);
                    DadosTemporada dadosTemporada = converteDadosJson.obterDados(jsonTemporada, DadosTemporada.class);
                    temporadas.add(dadosTemporada);
                }
                salvarEpisodiosDaSerieNoBanco(serie, temporadas);
            }
        }

        return episodioRepository.findBySerieIdOrderByTemporadaAscNumeroEpisodioAsc(serie.getId());
    }
}
