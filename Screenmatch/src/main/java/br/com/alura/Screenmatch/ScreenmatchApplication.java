package br.com.alura.Screenmatch;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import br.com.alura.Screenmatch.Principal.principal;
import br.com.alura.Screenmatch.Repository.SerieRepository;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {

    private final SerieRepository repository;

    public ScreenmatchApplication(SerieRepository repository) {
        this.repository = repository;
    }

    public static void main(String[] args) {
        SpringApplication.run(ScreenmatchApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        principal principal = new principal(repository);
        principal.exibeMenu();
    }
}