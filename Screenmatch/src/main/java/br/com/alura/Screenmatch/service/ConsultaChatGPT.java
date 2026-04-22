package br.com.alura.Screenmatch.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.errors.RateLimitException;
import com.openai.errors.UnauthorizedException;
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

        OpenAIClient cliente = getClient();
        if (cliente == null) {
            return texto;
        }

        ChatCompletionCreateParams requisicao = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addUserMessage("Traduza para o portugues o texto abaixo, sem explicar e sem adicionar aspas:\n" + texto)
                .build();

        try {
            return cliente.chat()
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
        } catch (UnauthorizedException e) {
            chatGptDisponivel = false;
            System.out.println("OpenAI KEY ausente ou invalida. Usando a sinopse original.");
            return texto;
        }
    }

    private static synchronized OpenAIClient getClient() {
        if (client == null) {
            String apiKey = obterApiKey();
            if (apiKey == null) {
                chatGptDisponivel = false;
                return null;
            }

            client = OpenAIOkHttpClient.builder()
                    .apiKey(apiKey)
                    .build();
        }
        return client;
    }

    private static String obterApiKey() {
        String apiKey = System.getenv(OPENAI_API_KEY);
        if (apiKeyValida(apiKey)) {
            return apiKey;
        }

        apiKey = System.getProperty("openai.apiKey");
        if (apiKeyValida(apiKey)) {
            return apiKey;
        }

        Path arquivoEnv = localizarArquivoEnv();
        if (arquivoEnv != null) {
            try {
                for (String linha : Files.readAllLines(arquivoEnv)) {
                    String linhaTratada = linha.trim();
                    if (linhaTratada.startsWith(OPENAI_API_KEY + "=")) {
                        String valor = linhaTratada.substring((OPENAI_API_KEY + "=").length()).trim();
                        valor = removerAspas(valor);
                        if (apiKeyValida(valor)) {
                            return valor;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Nao foi possivel ler o arquivo .env. Seguindo sem traducao por OpenAI.");
                return null;
            }
        }

        return null;
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

    private static boolean apiKeyValida(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }

        String valorNormalizado = apiKey.trim();
        return !valorNormalizado.equalsIgnoreCase("Chave-aqui")
                && !valorNormalizado.equalsIgnoreCase("sua_chave_aqui");
    }
}
