package cooperpay.fx;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LojaApiClient {

    private static final String BASE_URL = "http://localhost:8080/lojas";
    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{[^}]*\\}");
    private static final Pattern ID_PATTERN = Pattern.compile("\"lojaId\"\\s*:\\s*(\\d+)");
    private static final Pattern NOME_PATTERN = Pattern.compile("\"nome\"\\s*:\\s*\"([^\"]*)\"");

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public List<LojaItem> listar() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha ao listar lojas. HTTP " + response.statusCode());
        }

        return parseLista(response.body());
    }

    public void criar(String nome) throws IOException, InterruptedException {
        String body = "{\"nome\":\"" + escapeJson(nome) + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha ao criar loja. HTTP " + response.statusCode());
        }
    }

    public void excluir(Long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha ao excluir loja. HTTP " + response.statusCode());
        }
    }

    private List<LojaItem> parseLista(String json) {
        List<LojaItem> itens = new ArrayList<>();
        Matcher objectMatcher = OBJECT_PATTERN.matcher(json);
        while (objectMatcher.find()) {
            String obj = objectMatcher.group();
            Long id = parseId(obj);
            String nome = parseString(obj, NOME_PATTERN);
            if (id != null) {
                itens.add(new LojaItem(id, nome));
            }
        }
        return itens;
    }

    private Long parseId(String obj) {
        Matcher m = ID_PATTERN.matcher(obj);
        if (!m.find()) {
            return null;
        }
        return Long.parseLong(m.group(1));
    }

    private String parseString(String obj, Pattern pattern) {
        Matcher m = pattern.matcher(obj);
        if (!m.find()) {
            return "";
        }
        return m.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
