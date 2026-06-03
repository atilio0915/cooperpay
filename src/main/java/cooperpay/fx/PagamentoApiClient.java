package cooperpay.fx;

import java.math.BigDecimal;
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

import cooperpay.dto.PagamentoDTOResponse;

public class PagamentoApiClient {

    private static final String BASE_URL = "http://localhost:8080/pagamentos";
    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{[^}]*\\}");
    private static final Pattern ID_PAGAMENTO_PATTERN = Pattern.compile("\"idPagamento\"\\s*:\\s*(\\d+)");
    private static final Pattern ID_MOTOBOY_PATTERN = Pattern.compile("\"idMotoboy\"\\s*:\\s*(\\d+)");
    private static final Pattern NOME_PATTERN = Pattern.compile("\"nomeMotoboy\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern PIX_PATTERN = Pattern.compile("\"pixMotoboy\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern TRABALHOU_PATTERN = Pattern.compile("\"trabalhou\"\\s*:\\s*(true|false|null)");
    private static final Pattern STATUS_PATTERN = Pattern.compile("\"status\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern SEMANA_PATTERN = Pattern.compile("\"semanaReferencia\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern TXID_PATTERN = Pattern.compile("\"txidPix\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern VALOR_PATTERN = Pattern.compile("\"valor\"\\s*:\\s*([0-9.]+)");
    private static final Pattern SALVO_PATTERN = Pattern.compile("\"salvo\"\\s*:\\s*(true|false|null)");

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void criarRascunho(Long idMotoboy, String semanaReferencia) throws IOException, InterruptedException {
        String body = "{\"idMotoboy\":" + idMotoboy + ",\"semanaReferencia\":\"" + escapeJson(semanaReferencia) + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/rascunho"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha ao criar rascunho. HTTP " + response.statusCode());
        }
    }

    public void removerRascunho(Long idMotoboy, String semanaReferencia) throws IOException, InterruptedException {
        String url = BASE_URL + "/rascunho?motoboyId=" + idMotoboy
                + "&semanaReferencia=" + URLEncoder.encode(semanaReferencia, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha ao remover rascunho. HTTP " + response.statusCode());
        }
    }

    public List<PagamentoDTOResponse> listarRascunhos(String semanaReferencia) throws IOException, InterruptedException {
        String url = BASE_URL + "?status=RASCUNHO&semanaReferencia="
                + URLEncoder.encode(semanaReferencia, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha ao listar rascunhos. HTTP " + response.statusCode());
        }

        return parseLista(response.body());
    }

    public List<PagamentoDTOResponse> listarPagos(String dataIso) throws IOException, InterruptedException {
        // Removemos o filtro fixo de status para trazer PAGO e FALHOU
        StringBuilder url = new StringBuilder(BASE_URL).append("?ignoreRascunho=true"); 
        
        if (dataIso != null && !dataIso.isBlank()) {
            url.append("&data=").append(URLEncoder.encode(dataIso, StandardCharsets.UTF_8));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha ao listar pagamentos pagos. HTTP " + response.statusCode());
        }

        return parseLista(response.body());
    }

    public void atualizarValor(Long idPagamento, BigDecimal valor) throws IOException, InterruptedException {
        String url = BASE_URL + "/" + idPagamento + "/valor?valor="
                + URLEncoder.encode(valor.toPlainString(), StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha ao atualizar valor do rascunho. HTTP " + response.statusCode());
        }
    }

    public void pagarRascunho(Long idPagamento) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + idPagamento + "/pagar"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha ao pagar rascunho. HTTP " + response.statusCode());
        }
    }

    private List<PagamentoDTOResponse> parseLista(String json) {
        List<PagamentoDTOResponse> itens = new ArrayList<>();
        Matcher objectMatcher = OBJECT_PATTERN.matcher(json);
        while (objectMatcher.find()) {
            String obj = objectMatcher.group();
            itens.add(new PagamentoDTOResponse(
                    parseLong(obj, ID_PAGAMENTO_PATTERN),
                    parseLong(obj, ID_MOTOBOY_PATTERN),
                    parseString(obj, NOME_PATTERN),
                    parseString(obj, PIX_PATTERN),
                    parseTrabalhou(obj),
                    parseValor(obj),
                    parseString(obj, SEMANA_PATTERN),
                    parseString(obj, STATUS_PATTERN),
                    parseString(obj, TXID_PATTERN),
                    parseSalvo(obj)));
        }
        return itens;
    }

    private Long parseLong(String obj, Pattern pattern) {
        Matcher m = pattern.matcher(obj);
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

    private BigDecimal parseValor(String obj) {
        Matcher m = VALOR_PATTERN.matcher(obj);
        if (!m.find()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(m.group(1));
    }

    private Boolean parseTrabalhou(String obj) {
        Matcher m = TRABALHOU_PATTERN.matcher(obj);
        if (!m.find()) {
            return false;
        }
        return "true".equalsIgnoreCase(m.group(1));
    }

    private Boolean parseSalvo(String obj) {
        Matcher m = SALVO_PATTERN.matcher(obj);
        if (!m.find()) {
            return false;
        }
        return "true".equalsIgnoreCase(m.group(1));
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String unescapeJson(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
