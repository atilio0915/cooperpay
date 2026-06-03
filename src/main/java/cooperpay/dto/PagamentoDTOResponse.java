package cooperpay.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class PagamentoDTOResponse {
    private Long idPagamento;
    private Long idMotoboy;
    private String nomeMotoboy;
    private String pixMotoboy;
    private Boolean trabalhou;
    private BigDecimal valor;
    private String semanaReferencia;
    private String status;
    private String txidPix;
    private Boolean salvo;

    public String getTrabalhouTexto() {
        return Boolean.TRUE.equals(trabalhou) ? "Sim" : "Nao";
    }

    public String getSalvoTexto() {
        return Boolean.TRUE.equals(salvo) ? "Sim" : "Nao";
    }
}
