package cooperpay.dto;

import java.time.LocalDate;

import cooperpay.domain.EnumStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagamentoFiltroDto {
    private Long idMotoboy;
    private String semanaReferencia;
    private EnumStatus status;
    private LocalDate dataPagamento;
}
