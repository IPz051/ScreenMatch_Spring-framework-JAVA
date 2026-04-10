package br.com.alura.Screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DadosEpisodio(@JsonAlias("Title") String titulo , 
                            @JsonProperty("Season") Integer temporada , 
                            @JsonProperty("Episode") Integer episodio , 
                            @JsonProperty("imdbRating") String avaliacao) {

}
