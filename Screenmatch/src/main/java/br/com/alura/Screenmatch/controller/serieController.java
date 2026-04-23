package br.com.alura.Screenmatch.controller;


import br.com.alura.Screenmatch.DTO.serieDTO;
import br.com.alura.Screenmatch.service.SerieService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;




@RestController
@RequestMapping("/series")
public class serieController {

    
    @Autowired
    private SerieService serieService;

    record NovaSerieRequest(String titulo) {
    }
    record EpisodioDTO(long id, Integer temporada, Integer numeroEpisodio, String titulo, double avaliacao) {
    }

    @GetMapping({ "", "/" })
    public List<serieDTO> obterSeries () {
        return serieService.obterTodasAsSeries();
    }

    @GetMapping("/top5")
    public List<serieDTO> obterTop5Series () {
        return serieService.obterTop5Series();
    }

    @PostMapping({ "", "/" })
    public serieDTO adicionarSerie(@RequestBody NovaSerieRequest request) {
        String titulo = request == null ? null : request.titulo();
        return serieService.adicionarSerie(titulo);
    }

    @GetMapping("/lancamentos")
    public List<serieDTO> obterLancamentos () {
        return serieService.obterLancamentos();
    }

    @GetMapping("/{id}")
    public serieDTO obterSerie(@PathVariable Long id) {
        return serieService.obterSerie(id);
    }

    @GetMapping("/{id}/episodios")
    public List<EpisodioDTO> obterEpisodiosDaSerie(@PathVariable Long id) {
        return serieService.obterEpisodiosDaSerie(id).stream()
                .map(e -> new EpisodioDTO(
                        e.getId(),
                        e.getTemporada(),
                        e.getNumeroEpisodio(),
                        e.getTitulo(),
                        e.getAvaliacao()))
                .toList();
    }
    
}
