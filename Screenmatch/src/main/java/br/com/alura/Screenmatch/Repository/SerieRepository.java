package br.com.alura.Screenmatch.Repository;

import br.com.alura.Screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;

// Interface para operações de banco de dados de séries , salvar, buscar, atualizar e excluir
public interface SerieRepository extends JpaRepository<Serie, Long> {

}
