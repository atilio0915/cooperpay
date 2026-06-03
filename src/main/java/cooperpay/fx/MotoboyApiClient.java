package cooperpay.fx;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MotoboyApiClient {

    private static final String BASE_URL = "http://localhost:8080/motoboy";
    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{[^}]*\\}");
    private static final Pattern ID_PATTERN = Pattern.compile("\"motoboyId\"\\s*:\\s*(\\d+)");
    private static final Pattern NOME_PATTERN = Pattern.compile("\"nome\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern PIX_PATTERN = Pattern.compile("\"pix\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern TRABALHOU_PATTERN = Pattern.compile("\"trabalhou\"\\s*:\\s*(true|false|null)");
    private static final Pattern LOJA_ID_PATTERN = Pattern.compile("\"lojaId\"\\s*:\\s*(\\d+|null)");
    private static final Pattern LOJA_NOME_PATTERN = Pattern.compile("\"lojaNome\"\\s*:\\s*\"([^\"]*)\"");

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public List<MotoboyItem> listar(String nomeFiltro) throws IOException, InterruptedException {
        return listar(nomeFiltro, null);
    }

    public List<MotoboyItem> listar(String nomeFiltro, Long lojaId) throws IOException, InterruptedException {
        String filtro = nomeFiltro == null ? "" : nomeFiltro.trim();
        StringBuilder url = new StringBuilder(BASE_URL)
                .append("?nome=")
                .append(URLEncoder.encode(filtro, StandardCharsets.UTF_8));

        if (lojaId != null) {
            url.append("&lojaId=").append(lojaId);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha ao listar motoboys. HTTP " + response.statusCode());
        }

        return parseLista(response.body());
    }

    public void criar(String nome, String pix) throws IOException, InterruptedException {
        String body = "{\"nome\":\"" + escapeJson(nome) + "\",\"pix\":\"" + escapeJson(pix) + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha ao criar motoboy. HTTP " + response.statusCode());
        }
    }

    public void excluir(Long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha ao excluir motoboy. HTTP " + response.statusCode());
        }
    }

    public void atualizar(Long id, String nome, String pix) throws IOException, InterruptedException {
        atualizar(id, nome, pix, null, null);
    }

    public void atualizar(Long id, String nome, String pix, Boolean trabalhou) throws IOException, InterruptedException {
        atualizar(id, nome, pix, trabalhou, null);
    }

    public void atualizar(Long id, String nome, String pix, Boolean trabalhou, Long lojaId) throws IOException, InterruptedException {
        String trabalhouJson = trabalhou == null ? "null" : trabalhou.toString();
        String lojaIdJson = lojaId == null ? "null" : lojaId.toString();
        String body = "{\"nome\":\"" + escapeJson(nome)
                + "\",\"pix\":\"" + escapeJson(pix)
                + "\",\"trabalhou\":" + trabalhouJson
                + ",\"lojaId\":" + lojaIdJson
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha ao atualizar motoboy. HTTP " + response.statusCode());
        }
    }

    private List<MotoboyItem> parseLista(String json) {
        List<MotoboyItem> itens = new ArrayList<>();
        Matcher objectMatcher = OBJECT_PATTERN.matcher(json);
        while (objectMatcher.find()) {
            String obj = objectMatcher.group();
            Long id = parseId(obj);
            String nome = parseString(obj, NOME_PATTERN);
            String pix = parseString(obj, PIX_PATTERN);
            Boolean trabalhou = parseTrabalhou(obj);
            Long lojaId = parseNullableLong(obj, LOJA_ID_PATTERN);
            String lojaNome = parseString(obj, LOJA_NOME_PATTERN);
            itens.add(new MotoboyItem(id, nome, pix, trabalhou, lojaId, lojaNome));
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
        return unescapeJson(m.group(1));
    }

    private Boolean parseTrabalhou(String obj) {
        Matcher m = TRABALHOU_PATTERN.matcher(obj);
        if (!m.find()) {
            return false;
        }
        String valor = m.group(1);
        return "true".equalsIgnoreCase(valor);
    }

    private Long parseNullableLong(String obj, Pattern pattern) {
        Matcher m = pattern.matcher(obj);
        if (!m.find()) {
            return null;
        }
        String valor = m.group(1);
        if ("null".equalsIgnoreCase(valor)) {
            return null;
        }
        return Long.parseLong(valor);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String unescapeJson(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
