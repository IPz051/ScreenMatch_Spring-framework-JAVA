# Screenmatch

Aplicacao Java com Spring Boot que consome a API OMDb para buscar series e exibir informacoes no terminal.

## Objetivo

O projeto demonstra um fluxo simples de:

- leitura de dados digitados pelo usuario no terminal;
- consumo de API HTTP com `HttpClient`;
- conversao de JSON para objetos Java com Jackson;
- exibicao organizada das informacoes de series, temporadas e episodios.

## Funcionalidades atuais

- buscar uma serie pelo nome;
- exibir ficha da serie com titulo, temporadas, avaliacao, votos, genero, atores, poster e sinopse;
- listar os 5 episodios com maior avaliacao IMDb;
- buscar episodio por parte do titulo;
- listar todos os episodios por temporada;
- calcular media da temporada;
- mostrar estatisticas simples por temporada;
- armazenar e listar as series ja buscadas na sessao.

## Tecnologias

- Java 17
- Spring Boot
- Maven Wrapper
- Jackson Databind
- API OMDb

## Estrutura principal

```text
src/main/java/br/com/alura/Screenmatch
|- ScreenmatchApplication.java
|- Principal/principal.java
|- model/
|  |- DadosSerie.java
|  |- DadosTemporada.java
|  |- DadosEpisodio.java
|  |- Serie.java
|  `- CategoriaEnum.java
`- service/
   |- ConsumoApi.java
   |- ConverteDados.java
   `- IConverteDados.java
```

## Fluxo da aplicacao

1. `ScreenmatchApplication` inicia a aplicacao Spring Boot.
2. O metodo `run()` cria a classe `principal`.
3. `principal.exibeMenu()` mostra o menu no terminal.
4. Ao buscar uma serie, o projeto consulta a API OMDb.
5. O JSON retornado e convertido para objetos Java.
6. Os dados sao exibidos no terminal e a serie fica salva na lista de buscas.

## Menu atual

```text
1 - Buscar serie
2 - Listar series buscadas
3 - Sair
```

## Como executar

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux ou macOS

```bash
./mvnw spring-boot:run
```

## Como testar

```powershell
.\mvnw.cmd test
```

## Observacoes

- A chave da API OMDb esta definida diretamente na classe `principal`.
- As series buscadas ficam em memoria apenas durante a execucao atual.
- A interface da aplicacao e feita pelo terminal, sem pagina web.

## Possiveis proximos passos

- mover a chave da API para `application.properties` ou variavel de ambiente;
- renomear a classe `principal` para `Principal` para seguir a convencao do Java;
- separar melhor as responsabilidades entre menu, servico e camada de modelo;
- adicionar mais testes automatizados.
