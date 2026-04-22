package br.com.alura.Screenmatch.Repository;

import java.util.List;
import java.util.Optional;

import br.com.alura.Screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;

// Interface para operações de banco de dados de séries , salvar, buscar, atualizar e excluir
public interface SerieRepository extends JpaRepository<Serie, Long> {
    Optional<Serie> findByTituloIgnoreCase(String titulo);
    List<Serie> findByAtoresContainingIgnoreCase(String ator);
}
