package br.com.alura.Screenmatch.DTO;

import java.time.LocalDate;

import br.com.alura.Screenmatch.model.CategoriaEnum;

public record serieDTO(

        long id,

        String titulo,
        Integer temporada,
        double avaliacao,

        CategoriaEnum genero,
        String atores,
        String poster,
        String sinopse,
        LocalDate dataLancamento) {

}
