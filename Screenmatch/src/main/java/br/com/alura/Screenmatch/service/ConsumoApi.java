package br.com.alura.Screenmatch.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConsumoApi {                           // Classe para consumir a API OMDB

    public String obterDados(String endereco) {     // Metodo para obter dados da API
    HttpClient client = HttpClient.newHttpClient(); // Cria um novo cliente HTTP
    HttpRequest request = HttpRequest.newBuilder()  // Cria uma nova requisição HTTP
            .uri(URI.create(endereco))              // Define a URI da requisição
            .build();                               // Constroi a requisição
    HttpResponse<String> response = null;
    try {
        response = client
                .send(request, HttpResponse.BodyHandlers.ofString()); // Envia a requisição e obtém a resposta
    } catch (IOException e) {
        throw new RuntimeException(e); // Lança uma exceção runtime com a causa original
    } catch (InterruptedException e) {
        throw new RuntimeException(e); 
    }

    String json = response.body(); // Obtém o corpo da resposta como uma string
    return json;
}
    
}
