package br.com.alura.Screenmatch.service;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ConverteDados implements IConverteDados {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> T obterDados(String json, Class<T> classe) {
        try {
            return objectMapper.readValue(json, classe);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter JSON em objeto Java", e);
        }
    }
}
