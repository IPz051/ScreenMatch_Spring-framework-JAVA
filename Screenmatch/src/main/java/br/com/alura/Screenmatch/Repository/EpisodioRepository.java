package br.com.alura.Screenmatch.Repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.alura.Screenmatch.model.Episodio;

public interface EpisodioRepository extends JpaRepository<Episodio, Long> {
    long countBySerieId(long serieId);

    void deleteBySerieId(long serieId);

    List<Episodio> findBySerieIdOrderByTemporadaAscNumeroEpisodioAsc(long serieId);

    List<Episodio> findBySerieTituloIgnoreCaseOrderByTemporadaAscNumeroEpisodioAsc(String titulo);

    @Query("""
            SELECT e
            FROM Episodio e
            WHERE LOWER(e.titulo) LIKE LOWER(CONCAT('%', :trecho, '%'))
            ORDER BY e.serie.titulo, e.temporada, e.numeroEpisodio
            """)
    List<Episodio> episodiosPorTrecho(@Param("trecho") String trecho);

    @Query("""
            SELECT e
            FROM Episodio e
            WHERE LOWER(e.serie.titulo) = LOWER(:tituloSerie)
              AND e.avaliacao > 0
            ORDER BY e.avaliacao DESC
            """)
    List<Episodio> topEpisodiosPorSerie(@Param("tituloSerie") String tituloSerie, Pageable pageable);
}
