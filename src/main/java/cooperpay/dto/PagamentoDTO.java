package cooperpay.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagamentoDTO {
    private Long idMotoboy;
    private BigDecimal valor;
    private String semanaReferencia;
}
