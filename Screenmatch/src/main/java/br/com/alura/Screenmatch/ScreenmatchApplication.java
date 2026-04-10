package br.com.alura.Screenmatch;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import br.com.alura.Screenmatch.Principal.principal;

@SpringBootApplication // Classe principal da aplicação Spring Boot
public class ScreenmatchApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override // Método que será executado quando a aplicação for iniciada
	public void run(String... args) throws Exception {

		principal principal = new principal();
		principal.exibeMenu();

	}
}
