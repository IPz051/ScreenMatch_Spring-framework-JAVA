package br.com.alura.Screenmatch.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.alura.Screenmatch.model.Episodio;

public interface EpisodioRepository extends JpaRepository<Episodio, Long> {
    long countBySerieId(long serieId);

    void deleteBySerieId(long serieId);

    List<Episodio> findBySerieIdOrderByTemporadaAscNumeroEpisodioAsc(long serieId);

    List<Episodio> findBySerieTituloIgnoreCaseOrderByTemporadaAscNumeroEpisodioAsc(String titulo);
}
