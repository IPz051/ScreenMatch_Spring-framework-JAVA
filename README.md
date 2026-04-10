# 07 - Spring Framework e Maven

Este diretório contém o projeto **Screenmatch**, uma aplicação Java com **Spring Boot** e **Maven**.

## Estrutura

- `Screenmatch/`
  - `pom.xml`: dependências e configuração do build Maven/Spring Boot.
  - `mvnw` / `mvnw.cmd` + `.mvn/`: Maven Wrapper (permite rodar Maven sem instalar globalmente).
  - `src/main/java/`: código-fonte.
  - `src/main/resources/application.properties`: propriedades da aplicação.
  - `target/`: artefatos gerados pelo build (classes compiladas e `.jar`).

## Como executar

Dentro da pasta `Screenmatch/`:

```powershell
.\mvnw.cmd -DskipTests spring-boot:run
```

Para apenas compilar/empacotar:

```powershell
.\mvnw.cmd -DskipTests package
```

## O que a aplicação faz (fluxo)

A aplicação sobe o contexto do Spring Boot e executa um **menu no terminal** (não é uma API web neste momento).

- A classe de entrada é [`ScreenmatchApplication`](file:///c:/Users/DELL/Desktop/Alura%20-%20Next.js%20%26%20TailwindCSS/Carreira%20JAVA/07%20-%20Spring%20Framework%20e%20Maven/Screenmatch/src/main/java/br/com/alura/Screenmatch/ScreenmatchApplication.java).
- Ela implementa `CommandLineRunner`, então o método `run(...)` é executado automaticamente quando o Spring inicia.
- No `run(...)`, ela instancia [`principal`](file:///c:/Users/DELL/Desktop/Alura%20-%20Next.js%20%26%20TailwindCSS/Carreira%20JAVA/07%20-%20Spring%20Framework%20e%20Maven/Screenmatch/src/main/java/br/com/alura/Screenmatch/Principal/principal.java) e chama `exibeMenu()`.
- No menu, você digita o nome de uma série e o programa:
  - chama a API OMDb para buscar dados gerais da série;
  - busca todas as temporadas (`season=1..N`);
  - lista episódios por temporada, um por linha;
  - calcula e exibe um “Top 5 episódios por avaliação IMDb”.

## Principais classes

### Entrada

- [`ScreenmatchApplication`](file:///c:/Users/DELL/Desktop/Alura%20-%20Next.js%20%26%20TailwindCSS/Carreira%20JAVA/07%20-%20Spring%20Framework%20e%20Maven/Screenmatch/src/main/java/br/com/alura/Screenmatch/ScreenmatchApplication.java): inicia o Spring Boot e chama o menu.
- [`principal`](file:///c:/Users/DELL/Desktop/Alura%20-%20Next.js%20%26%20TailwindCSS/Carreira%20JAVA/07%20-%20Spring%20Framework%20e%20Maven/Screenmatch/src/main/java/br/com/alura/Screenmatch/Principal/principal.java): implementa o menu e o fluxo de consulta (série/temporadas/episódios).

### Serviços

- [`ConsumoApi`](file:///c:/Users/DELL/Desktop/Alura%20-%20Next.js%20%26%20TailwindCSS/Carreira%20JAVA/07%20-%20Spring%20Framework%20e%20Maven/Screenmatch/src/main/java/br/com/alura/Screenmatch/service/ConsumoApi.java): faz requisições HTTP e retorna o JSON como `String`.
- [`ConverteDados`](file:///c:/Users/DELL/Desktop/Alura%20-%20Next.js%20%26%20TailwindCSS/Carreira%20JAVA/07%20-%20Spring%20Framework%20e%20Maven/Screenmatch/src/main/java/br/com/alura/Screenmatch/service/ConverteDados.java): converte JSON em objetos Java usando Jackson (`ObjectMapper`).
- [`IConverteDados`](file:///c:/Users/DELL/Desktop/Alura%20-%20Next.js%20%26%20TailwindCSS/Carreira%20JAVA/07%20-%20Spring%20Framework%20e%20Maven/Screenmatch/src/main/java/br/com/alura/Screenmatch/service/IConverteDados.java): interface do conversor.

### Modelos (records)

- [`DadosSerie`](file:///c:/Users/DELL/Desktop/Alura%20-%20Next.js%20%26%20TailwindCSS/Carreira%20JAVA/07%20-%20Spring%20Framework%20e%20Maven/Screenmatch/src/main/java/br/com/alura/Screenmatch/model/DadosSerie.java): dados gerais da série (título, total de temporadas, nota, votos).
- [`DadosTemporada`](file:///c:/Users/DELL/Desktop/Alura%20-%20Next.js%20%26%20TailwindCSS/Carreira%20JAVA/07%20-%20Spring%20Framework%20e%20Maven/Screenmatch/src/main/java/br/com/alura/Screenmatch/model/DadosTemporada.java): número da temporada e lista de episódios.
- [`DadosEpisodio`](file:///c:/Users/DELL/Desktop/Alura%20-%20Next.js%20%26%20TailwindCSS/Carreira%20JAVA/07%20-%20Spring%20Framework%20e%20Maven/Screenmatch/src/main/java/br/com/alura/Screenmatch/model/DadosEpisodio.java): dados do episódio (título, temporada, número do episódio e avaliação).

## Dependências (pom.xml)

Em [`pom.xml`](file:///c:/Users/DELL/Desktop/Alura%20-%20Next.js%20%26%20TailwindCSS/Carreira%20JAVA/07%20-%20Spring%20Framework%20e%20Maven/Screenmatch/pom.xml):

- `spring-boot-starter`: núcleo do Spring Boot.
- `spring-boot-starter-actuator`: endpoints de observabilidade/health (mesmo sem “web”, é inicializado no contexto).
- `spring-boot-starter-test`: testes.
- `jackson-databind`: conversão JSON ↔ objetos.
- `jackson-datatype-jsr310`: suporte do Jackson para `java.time.*`.

## Configuração

- [`application.properties`](file:///c:/Users/DELL/Desktop/Alura%20-%20Next.js%20%26%20TailwindCSS/Carreira%20JAVA/07%20-%20Spring%20Framework%20e%20Maven/Screenmatch/src/main/resources/application.properties): define `spring.application.name=Screenmatch`.

## Observações

- A aplicação depende de internet para consultar a OMDb.
- A chave da OMDb está embutida no código na classe `principal` (como constante `API_KEY`). Se você quiser evoluir o projeto, o caminho usual é mover isso para `application.properties` e ler via configuração do Spring.
