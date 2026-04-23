package br.com.alura.Screenmatch.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import br.com.alura.Screenmatch.model.CategoriaEnum;
import br.com.alura.Screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Interface para operações de banco de dados de séries , salvar, buscar, atualizar e excluir
public interface SerieRepository extends JpaRepository<Serie, Long> {
    Optional<Serie> findByTituloIgnoreCase(String titulo);
    List<Serie> findByAtoresContainingIgnoreCase(String ator);
    List<Serie> findByGenero(CategoriaEnum genero);
    List<Serie> findTop5ByOrderByAvaliacaoDesc();

    @Query("""
            SELECT s
            FROM Serie s
            WHERE s.temporada >= :temporadas
              AND s.avaliacao >= :avaliacao
            ORDER BY s.avaliacao DESC
            """)
    List<Serie> seriesPorTemporadaEAvaliacao(@Param("temporadas") Integer temporadas,
            @Param("avaliacao") Double avaliacao);

    @Query("""
            SELECT s
            FROM Serie s
            WHERE s.dataLancamento IS NOT NULL
              AND s.dataLancamento BETWEEN :inicio AND :fim
            ORDER BY s.dataLancamento DESC
            """)
    List<Serie> seriesPorDataLancamento(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
    @Query("""
            SELECT s
            FROM Serie s
            WHERE s.dataLancamento IS NOT NULL
            ORDER BY s.dataLancamento DESC
            """)
    List<Serie> seriesPorDataLancamentoDesc();
}
