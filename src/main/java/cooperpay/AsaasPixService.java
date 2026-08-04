package cooperpay.integration.asaas;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cooperpay.server.PixInterface;

@Service
public class AsaasPixService implements PixInterface {

    private final WebClient webClient;
    private final String accessToken;
    private final String descricaoPadrao;
    private final String baseUrl;
    private static final Logger log = LoggerFactory.getLogger(AsaasPixService.class);

    public AsaasPixService(
            WebClient.Builder webClientBuilder,
            @Value("${asaas.api.base-url:https://api.asaas.com/v3/}") String baseUrl,
            @Value("${asaas.api.access-token:}") String accessToken,
            @Value("${asaas.api.pix-descricao-padrao:Pagamento CooperPay}") String descricaoPadrao) {
        
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofSeconds(60)); // Aumentado para 60s devido a instabilidades no Sandbox

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("User-Agent", "cooperpay")
                .build();

        // Higienização robusta contra caracteres invisíveis e lixo de cópia
        String rawToken = accessToken != null ? accessToken.trim() : "";
        
        // Remove BOM UTF-8 e caracteres de controle
        rawToken = rawToken.replaceAll("[\\p{Cf}\\p{Co}\\p{Cn}]", "").trim();

        this.accessToken = rawToken.replaceAll("[\\s\\p{Cntrl}]", "");

        this.descricaoPadrao = descricaoPadrao;
        this.baseUrl = baseUrl;

        // Log para verificar se a chave correta está sendo carregada pelo servidor no boot
        if (this.accessToken.length() > 20) {
            String prefix = this.accessToken.substring(0, 15);
            String suffix = this.accessToken.substring(this.accessToken.length() - 5);
            log.debug("[Asaas] Service inicializado. Token: {}...{} | Comprimento: {}", prefix, suffix, this.accessToken.length());
        } else {
            log.warn("[Asaas] Service inicializado. TOKEN INVALIDO OU CURTO.");
        }
        exibirStatusToken("Inicializacao");
    }

    @Override
    public String enviarPix(String chavePix, BigDecimal valor, String idLocal) {
        String tipoChave = identificarTipoChave(chavePix);
        String chaveFormatada = chavePix.trim();

        // Asaas exige que chaves numéricas (CPF, CNPJ, Telefone) sejam enviadas apenas com dígitos
        if ("PHONE".equals(tipoChave) || "CPF".equals(tipoChave) || "CNPJ".equals(tipoChave)) {
            chaveFormatada = chaveFormatada.replaceAll("\\D", "");
            if ("PHONE".equals(tipoChave) && !chaveFormatada.startsWith("55")) {
                chaveFormatada = "55" + chaveFormatada;
            }
        }

        log.info("[Asaas] --- NOVA TENTATIVA DE TRANSFERENCIA ---");
        log.info("[Asaas] URL: {}transfers", baseUrl);
        log.debug("[Asaas] Destino: {} | Tipo: {} | Valor: R$ {}", chaveFormatada, tipoChave, valor);
        
        String tokenVisible = (accessToken != null && accessToken.length() > 10) ? accessToken.substring(0, 5) + "..." + accessToken.substring(accessToken.length() - 5) : "INVALIDO";
        log.debug("[Asaas] Token enviado no Header: [{}] | Comprimento: {}", tokenVisible, (accessToken != null ? accessToken.length() : 0));

        exibirStatusToken("Envio Pix");

        Map<String, Object> requestBody = Map.of(
            "value", valor,
            "pixAddressKey", chaveFormatada,
            "pixAddressKeyType", tipoChave,
            "description", descricaoPadrao,
            "operationType", "PIX",
            "externalReference", idLocal
        );
        log.debug("[Asaas] Request Body: {}", requestBody);

        Map response = webClient.post()
                .uri("transfers")
                .header("access_token", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                    clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                        if (clientResponse.statusCode().value() == 403) {
                            log.error("[Asaas] ACESSO NEGADO (403). Verifique se o seu IP atual está na Whitelist do painel Asaas.");
                        }
                        log.error("[Asaas] ERRO HTTP {}. Resposta do Servidor: {}", clientResponse.statusCode(), errorBody);
                        return reactor.core.publisher.Mono.error(new RuntimeException("Erro na API Asaas: " + clientResponse.statusCode()));
                    })
                )
                .bodyToMono(Map.class)
                .retryWhen(reactor.util.retry.Retry.fixedDelay(3, Duration.ofSeconds(5))
                    .filter(throwable -> {
                        String msg = throwable.getMessage();
                        return msg != null && (msg.contains("504") || msg.contains("502") || msg.contains("Timeout"));
                    }))
                .block();

        if (response == null || !response.containsKey("id")) {
            log.error("[Asaas] Resposta recebida porem invalida (sem ID): {}", response);
            throw new RuntimeException("Falha ao processar transferencia na Asaas");
        }

        log.info("[Asaas] Transferencia concluida com sucesso. ID Asaas: {}", response.get("id"));
        return (String) response.get("id");
    }

    @Override
    public String consultarStatus(String idAsaas) {
        if (idAsaas == null) return null;

        try {
            Map response = webClient.get()
                    .uri("transfers/{id}", idAsaas)
                    .header("access_token", accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.debug("[Asaas] Consulta de status ID {}: {}", idAsaas, response != null ? response.get("status") : "NULL");
            return response != null ? (String) response.get("status") : null;
        } catch (Exception e) {
            log.error("[Asaas] Erro ao consultar status da transferencia {}: {}", idAsaas, e.getMessage());
            return null;
        }
    }

    private void exibirStatusToken(String contexto) {
        if (this.accessToken != null && this.accessToken.length() > 20) {
            String inicio = this.accessToken.substring(0, 10);
            String fim = this.accessToken.substring(this.accessToken.length() - 10);
            log.debug("[Asaas] [{}] Token: {}...{} | Total: {}", contexto, inicio, fim, this.accessToken.length());
        } else {
            log.error("[Asaas] [{}] TOKEN INVALIDO OU AUSENTE NO SISTEMA!", contexto);
        }
    }

    private String identificarTipoChave(String chave) {
        if (chave == null) throw new IllegalArgumentException("Chave Pix nula");
        chave = chave.trim();

        // Identifica explicitamente Chave Aleatória (EVP) via padrão UUID
        if (chave.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            return "EVP";
        }
        
        if (chave.contains("@")) return "EMAIL";

        String apenasNumeros = chave.replaceAll("\\D", "");

        if (apenasNumeros.length() == 14) return "CNPJ";
        
        // Se começa com + ou tem 13 dígitos (já com 55), é telefone
        if (chave.startsWith("+") || apenasNumeros.length() == 13) return "PHONE";

        if (apenasNumeros.length() == 11) {
            // Se contém pontos, é CPF formatado
            if (chave.contains(".")) return "CPF";
            // Se contém parênteses ou se o terceiro dígito é 9 (ex: 119...), tratamos como celular
            if (chave.contains("(") || apenasNumeros.charAt(2) == '9') {
                return "PHONE";
            }
            return "PHONE";
        }

        // Se não se encaixou em nada acima (como chaves aleatórias com hifens), é EVP
        return "EVP";
    }
}