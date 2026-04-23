package br.com.alura.Screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//Serializar: ler o objeto Java e transformar em um JSON JSON
//Desserializar: ler o JSON e transformar em um objeto Java

//@JsonAlias Ler o atributo com o nome do atributo do JSON
//@JsonProperty Ler o atributo com o nome do atributo do objeto Java
//@JsonIgnoreProperties Ignorar os atributos que não estão mapeados no objeto Java

@JsonIgnoreProperties(ignoreUnknown = true)
public record DadosSerie(@JsonAlias("Title") String titulo,
        @JsonAlias("totalSeasons") Integer temporada,
        @JsonAlias("imdbRating") String avaliacao,
        @JsonProperty("imdbVotes") String votos,
        @JsonAlias("Genre") String genero,
        @JsonAlias("Actors") String atores,
        @JsonAlias("Poster") String poster,
        @JsonAlias("Plot") String sinopse,
        @JsonAlias("Released") String lancamento) {

}
