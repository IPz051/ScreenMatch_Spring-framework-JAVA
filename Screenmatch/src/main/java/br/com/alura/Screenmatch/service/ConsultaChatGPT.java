package br.com.alura.Screenmatch.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.errors.RateLimitException;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConsultaChatGPT {

    private static final String OPENAI_API_KEY = "OPENAI_API_KEY";
    private static OpenAIClient client;
    private static boolean chatGptDisponivel = true;

    public static String obterTraducao(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }

        if (!chatGptDisponivel) {
            return texto;
        }

        ChatCompletionCreateParams requisicao = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addUserMessage("Traduza para o portugues o texto abaixo, sem explicar e sem adicionar aspas:\n" + texto)
                .build();

        try {
            return getClient().chat()
                    .completions()
                    .create(requisicao)
                    .choices()
                    .stream()
                    .flatMap(choice -> choice.message().content().stream())
                    .findFirst()
                    .orElse(texto);
        } catch (RateLimitException e) {
            chatGptDisponivel = false;
            System.out.println("ChatGPT indisponivel no momento por falta de creditos. Usando a sinopse original.");
            return texto;
        }
    }

    private static synchronized OpenAIClient getClient() {
        if (client == null) {
            client = OpenAIOkHttpClient.builder()
                    .apiKey(obterApiKey())
                    .build();
        }
        return client;
    }

    private static String obterApiKey() {
        String apiKey = System.getenv(OPENAI_API_KEY);
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }

        apiKey = System.getProperty("openai.apiKey");
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }

        Path arquivoEnv = localizarArquivoEnv();
        if (arquivoEnv != null) {
            try {
                for (String linha : Files.readAllLines(arquivoEnv)) {
                    String linhaTratada = linha.trim();
                    if (linhaTratada.startsWith(OPENAI_API_KEY + "=")) {
                        String valor = linhaTratada.substring((OPENAI_API_KEY + "=").length()).trim();
                        return removerAspas(valor);
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Nao foi possivel ler o arquivo .env do projeto.", e);
            }
        }

        throw new IllegalStateException(
                "OPENAI_API_KEY nao encontrada. Defina a variavel de ambiente ou adicione a chave no arquivo .env do projeto.");
    }

    private static Path localizarArquivoEnv() {
        try {
            Path origem = Path.of(ConsultaChatGPT.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path diretorioAtual = Files.isRegularFile(origem) ? origem.getParent() : origem;

            for (Path diretorio = diretorioAtual; diretorio != null; diretorio = diretorio.getParent()) {
                Path arquivoEnv = diretorio.resolve(".env");
                if (Files.exists(arquivoEnv)) {
                    return arquivoEnv;
                }
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Nao foi possivel localizar a pasta do projeto.", e);
        }

        return null;
    }

    private static String removerAspas(String valor) {
        if (valor.length() >= 2 && valor.startsWith("\"") && valor.endsWith("\"")) {
            return valor.substring(1, valor.length() - 1);
        }
        return valor;
    }
}
