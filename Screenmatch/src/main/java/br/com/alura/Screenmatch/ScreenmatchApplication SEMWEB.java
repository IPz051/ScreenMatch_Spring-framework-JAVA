// package br.com.alura.Screenmatch;

// import org.springframework.boot.CommandLineRunner;
// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.core.env.Environment;

// import br.com.alura.Screenmatch.Principal.principal;
// import br.com.alura.Screenmatch.Repository.EpisodioRepository;
// import br.com.alura.Screenmatch.Repository.SerieRepository;

// @SpringBootApplication
// public class ScreenmatchApplication implements CommandLineRunner {

//     private final SerieRepository repository;
//     private final EpisodioRepository episodioRepository;
//     private final Environment environment;

//     public ScreenmatchApplication(SerieRepository repository, EpisodioRepository episodioRepository,
//             Environment environment) {
//         this.repository = repository;
//         this.episodioRepository = episodioRepository;
//         this.environment = environment;
//     } 

//     public static void main(String[] args) {
//         SpringApplication.run(ScreenmatchApplication.class, args);
//     }

//     @Override
//     public void run(String... args) throws Exception {
//         if (!environment.getProperty("screenmatch.cli.enabled", Boolean.class, true)) {
//             return;
//         }
//         principal principal = new principal(repository, episodioRepository);
//         principal.exibeMenu();
//     }
// }
