# Screenmatch

Aplicacao Java com Spring Boot que consome a API OMDb para buscar series e exibir informacoes no terminal. O projeto agora tambem usa JPA com PostgreSQL para persistir as series buscadas e integra a OpenAI para traduzir a sinopse quando houver chave e creditos disponiveis.

## Objetivo

O projeto demonstra um fluxo simples de:

- leitura de dados digitados pelo usuario no terminal;
- consumo de API HTTP com `HttpClient`;
- conversao de JSON para objetos Java com Jackson;
- exibicao organizada das informacoes de series, temporadas e episodios;
- persistencia de entidades com Spring Data JPA;
- uso da OpenAI para traducao de texto com fallback seguro.

## Funcionalidades atuais

- buscar uma serie pelo nome;
- exibir ficha da serie com titulo, temporadas, avaliacao, votos, genero, atores, poster e sinopse;
- traduzir a sinopse com OpenAI quando a chave estiver configurada e a conta tiver creditos;
- usar a sinopse original automaticamente quando o ChatGPT estiver indisponivel ou sem creditos;
- listar os 5 episodios com maior avaliacao IMDb;
- buscar episodio por parte do titulo;
- listar todos os episodios por temporada;
- calcular media da temporada;
- mostrar estatisticas simples por temporada;
- salvar series buscadas no PostgreSQL;
- listar as series salvas no banco.

## Atualizacoes de hoje

- troca da biblioteca antiga `com.theokanning.openai-gpt3-java` pela SDK oficial `com.openai:openai-java`;
- configuracao da leitura da `OPENAI_API_KEY` via variavel de ambiente, propriedade do sistema ou arquivo `.env`;
- tratamento do erro `429` da OpenAI para nao derrubar a aplicacao quando acabarem os creditos;
- adicao do `spring-boot-starter-data-jpa` e do driver `postgresql`;
- configuracao do datasource PostgreSQL em `application.properties`;
- criacao do repositorio `SerieRepository`;
- ajuste do fluxo para salvar series buscadas no banco;
- correcao da injecao do repositorio em `ScreenmatchApplication` e `principal`;
- ajuste da entidade `Serie` para funcionar melhor com JPA, incluindo construtor vazio.

## Tecnologias

- Java 17
- Spring Boot 4
- Spring Data JPA
- PostgreSQL
- Maven Wrapper
- Jackson Databind
- API OMDb
- OpenAI Java SDK

## Estrutura principal

```text
src/main/java/br/com/alura/Screenmatch
|- ScreenmatchApplication.java
|- Principal/principal.java
|- Repository/
|  `- SerieRepository.java
|- model/
|  |- CategoriaEnum.java
|  |- DadosEpisodio.java
|  |- DadosSerie.java
|  |- DadosTemporada.java
|  `- Serie.java
`- service/
   |- ConsumoApi.java
   |- ConsultaChatGPT.java
   |- ConverteDados.java
   `- IConverteDados.java
```

## Fluxo da aplicacao

1. `ScreenmatchApplication` inicia a aplicacao Spring Boot.
2. O metodo `run()` cria a classe `principal`, injetando o `SerieRepository`.
3. `principal.exibeMenu()` mostra o menu no terminal.
4. Ao buscar uma serie, o projeto consulta a API OMDb.
5. O JSON retornado e convertido para objetos Java.
6. A classe `Serie` tenta traduzir a sinopse via `ConsultaChatGPT`.
7. Se a OpenAI estiver sem creditos, a aplicacao usa a sinopse original e continua normalmente.
8. A serie e salva no PostgreSQL com `repository.save(...)`.
9. A opcao de listagem busca os dados salvos no banco com `repository.findAll()`.

## Menu atual

```text
1 - Buscar serie
2 - Listar series buscadas
3 - Sair
```

## Configuracao

### Banco de dados

O arquivo `src/main/resources/application.properties` contem a configuracao do PostgreSQL:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/screenmatch
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

Antes de executar, confirme que:

- o PostgreSQL esta em execucao;
- o banco `screenmatch` existe;
- usuario e senha estao corretos.

### OpenAI

A chave da OpenAI pode ser lida de tres formas:

- variavel de ambiente `OPENAI_API_KEY`;
- propriedade Java `openai.apiKey`;
- arquivo `.env` na raiz do projeto com:

```env
OPENAI_API_KEY="sua_chave_aqui"
```

Se a chave nao existir, a aplicacao informa o problema. Se a conta estiver sem creditos, a traducao e desativada durante a execucao atual e a sinopse original passa a ser usada.

## Como executar

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux ou macOS

```bash
./mvnw spring-boot:run
```

## Como instalar ou atualizar dependencias

```powershell
.\mvnw.cmd -U clean install
```

Ou, para compilar sem rodar os testes:

```powershell
.\mvnw.cmd -DskipTests package
```

Se o terminal mostrar `BUILD SUCCESS`, o Maven resolveu as dependencias do `pom.xml` corretamente.

## Como testar

Os testes atuais podem subir a aplicacao de console e travar esperando entrada do usuario. Para validar o build sem executar os testes:

```powershell
.\mvnw.cmd -DskipTests package
```

## Observacoes

- a chave da API OMDb ainda esta definida diretamente na classe `principal`;
- a listagem de series agora usa o banco de dados, nao apenas a memoria da sessao;
- a interface da aplicacao continua sendo feita pelo terminal, sem pagina web;
- o projeto ainda usa a classe `principal` com nome minusculo, embora o padrao Java recomende `Principal`.

## Possiveis proximos passos

- mover a chave da API OMDb para `application.properties` ou variavel de ambiente;
- renomear a classe `principal` para `Principal`;
- criar a entidade `Episodio` e mapear o relacionamento com `Serie`;
- melhorar os testes para o `mvn test` nao depender de entrada interativa;
- separar melhor as responsabilidades entre menu, servico e camada de persistencia.
