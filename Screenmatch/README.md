# Screenmatch

Aplicacao Java com Spring Boot que consome a API OMDb para buscar series e expor os dados via API REST. O projeto usa JPA com PostgreSQL para persistir as series/episodios e integra a OpenAI para traduzir a sinopse quando houver chave e creditos disponiveis. Ha tambem um front-end (Vite + JavaScript) para listar, filtrar e visualizar detalhes das series.

## Demo (deploy)

- Front-end (Vercel): https://screenmatchjava.vercel.app/
- Backend/API (Render): https://screenmatch-spring-framework-java.onrender.com

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
- exibir ficha da serie com titulo, temporadas, avaliacao, genero, atores, poster, sinopse e data de lancamento;
- traduzir a sinopse com OpenAI quando a chave estiver configurada e a conta tiver creditos;
- usar a sinopse original automaticamente quando o ChatGPT estiver indisponivel ou sem creditos;
- salvar series e episodios buscados no PostgreSQL;
- listar as series salvas no banco;
- listar episodios de uma serie usando o banco (carrega da API e persiste se ainda nao existir);
- buscar series por ator usando o banco;
- listar TOP 5 series por avaliacao;
- filtrar series por temporadas minimas e avaliacao minima (JPQL);
- buscar episodios por trecho do titulo (JPQL);
- listar TOP episodios por serie (JPQL);
- listar lancamentos (ordenado do mais recente para o mais antigo);
- front-end web com cards e modal exibindo temporadas e episodios.

## Mudancas recentes

- a aplicacao agora expoe endpoints REST para series, lancamentos, top 5 e episodios por serie;
- foi adicionado um front-end (Vite) em `src/main/resources/static` consumindo a API;
- foi incluido `dataLancamento` (OMDb `Released`) e listagem de lancamentos.

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
|- DTO/serieDTO.java
|- Repository/
|  |- EpisodioRepository.java
|  `- SerieRepository.java
|- config/corsConfiguration.java
|- controller/serieController.java
|- model/
|  |- CategoriaEnum.java
|  |- DadosEpisodio.java
|  |- DadosSerie.java
|  |- DadosTemporada.java
|  |- Episodio.java
|  `- Serie.java
`- service/
   |- ConsumoApi.java
   |- ConsultaChatGPT.java
   |- ConverteDados.java
   `- IConverteDados.java

src/main/resources/static
|- package.json
|- vite.config.js
`- src/
   |- main.js
   `- styles.css
```

## Fluxo da aplicacao

1. `ScreenmatchApplication` inicia a aplicacao Spring Boot (API REST).
2. O front-end (Vite) chama `GET /series` e renderiza os cards.
3. Ao adicionar uma serie pelo front (POST), o backend consulta a OMDb, salva a serie no banco e pode persistir episodios.
4. No clique do card, o front chama `GET /series/{id}/episodios` e exibe temporadas/episodios no modal.
5. A classe `Serie` tenta traduzir a sinopse via `ConsultaChatGPT` e usa fallback se necessario.

## API (endpoints)

- `GET /series` lista todas as series cadastradas (DTO).
- `POST /series` adiciona/atualiza uma serie a partir do titulo (busca na OMDb). Body: `{"titulo":"Breaking Bad"}`
- `GET /series/top5` retorna as 5 melhores series por avaliacao.
- `GET /series/lancamentos` retorna series ordenadas por data de lancamento (mais recente primeiro).
- `GET /series/{id}` retorna uma serie pelo id.
- `GET /series/{id}/episodios` retorna episodios da serie (ordenado por temporada/episodio) e carrega da OMDb se ainda nao existir no banco.

## Menu (CLI)

```text
1 - Buscar serie
2 - Listar series buscadas
3 - Listar episodios de uma serie
4 - Buscar series por ator
5 - TOP 5 series
6 - Listar series por categoria
7 - Series por temporadas e avaliacao
8 - Episodios por trecho do titulo
9 - TOP episodios por serie
0 - Sair
```

## Configuracao

### Banco de dados

O arquivo `src/main/resources/application.properties` contem a configuracao do PostgreSQL:

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

Antes de executar, confirme que:

- o PostgreSQL esta em execucao;
- as variaveis `DB_HOST`, `DB_NAME`, `DB_USER` e `DB_PASSWORD` estao definidas.

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

### Backend (API)

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux ou macOS

```bash
./mvnw spring-boot:run
```

### Front-end (Vite)

O front fica em `src/main/resources/static`:

```powershell
cd src/main/resources/static
npm install
npm run dev
```

O Vite usa proxy para o backend (por padrao `http://localhost:8080`). Ajuste se necessario em `vite.config.js`.

## Deploy

### Backend no Render

- Criar um Postgres no Render e configurar as variaveis `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.
- O backend usa `server.port=${PORT:8080}` para respeitar a porta definida pelo Render.

### Front na Vercel

- Root Directory: `Screenmatch/src/main/resources/static`
- Env var: `VITE_API_BASE=https://screenmatch-spring-framework-java.onrender.com`
- Build: `npm run build` (output `dist`)

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

Atualmente nao ha testes automatizados no repositorio, mas a dependencia `spring-boot-starter-test` esta no `pom.xml`.

Para validar apenas o build sem executar testes:

```powershell
.\mvnw.cmd -DskipTests package
```

## Observacoes

- a chave da API OMDb ainda esta definida diretamente na classe `principal`;
- a listagem de series agora usa o banco de dados, nao apenas a memoria da sessao;
- a API REST e consumida pelo front-end web (Vite);
- o projeto ainda usa a classe `principal` com nome minusculo, embora o padrao Java recomende `Principal`;
- o CORS esta configurado para `localhost/127.0.0.1` nas portas 5173/5501.

## Possiveis proximos passos

- mover a chave da API OMDb para `application.properties` ou variavel de ambiente;
- renomear a classe `principal` para `Principal`;
- melhorar os testes para o `mvn test` nao depender de entrada interativa;
- separar melhor as responsabilidades entre menu, servico e camada de persistencia.
